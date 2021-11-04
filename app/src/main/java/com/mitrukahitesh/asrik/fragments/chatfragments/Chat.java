package com.mitrukahitesh.asrik.fragments.chatfragments;

import static android.app.Activity.RESULT_OK;
import static android.content.Intent.ACTION_DIAL;

import android.accessibilityservice.AccessibilityService;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.inputmethodservice.Keyboard;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.adapters.Messages;
import com.mitrukahitesh.asrik.models.FileMetaData;
import com.mitrukahitesh.asrik.models.Message;
import com.mitrukahitesh.asrik.utility.Constants;
import com.mitrukahitesh.asrik.utility.FileDetails;
import com.mitrukahitesh.asrik.utility.TimeFormatter;

import java.io.File;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

import de.hdodenhof.circleimageview.CircleImageView;

public class Chat extends Fragment {

    private static final int MEDIA_SELECT = 1;
    private CircleImageView dp;
    private TextView name, lastSeen, fileName;
    private Bundle bundle = new Bundle();
    private String chatId = "";
    private EditText message;
    private ImageButton image, doc, send, remove, call;
    private long lastUpdate = 0;
    private boolean threadRun = true;
    private RecyclerView recyclerView;
    private Messages adapter;
    private LinearLayout fileLl;
    private Uri uri = null;
    private final List<Message> messages = new ArrayList<>();

    public Chat() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            bundle = getArguments();
        }
        if (FirebaseAuth.getInstance().getUid() != null)
            new Thread(() -> {
                Map<String, Object> map = new HashMap<>();
                map.put(Constants.MESSAGE, requireContext().getString(R.string.online));
                while (threadRun) {
                    if (System.currentTimeMillis() - lastUpdate > 2500) {
                        FirebaseFirestore.getInstance()
                                .collection(Constants.ONLINE)
                                .document(FirebaseAuth.getInstance().getUid())
                                .update(map);
                    }
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        threadRun = false;
    }

    @Override
    public void onResume() {
        super.onResume();
        requireActivity().findViewById(R.id.bottomAppBar).setVisibility(View.GONE);
        changeStatusBarColor(R.color.theme_color_light);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat2, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        requireActivity().getOnBackPressedDispatcher().addCallback(getViewLifecycleOwner(), new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Navigation.findNavController(view).popBackStack();
            }
        });
        setReferences(view);
        setListeners();
        setInfo();
        createChannelIdIfNotCreated();
    }

    private void setReferences(View view) {
        if (recyclerView != null) {
            adapter = (Messages) recyclerView.getAdapter();
        } else {
            adapter = new Messages(requireContext(), messages);
        }
        dp = view.findViewById(R.id.dp);
        name = view.findViewById(R.id.name);
        lastSeen = view.findViewById(R.id.last_seen);
        message = view.findViewById(R.id.message);
        recyclerView = view.findViewById(R.id.recycler);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        recyclerView.setAdapter(adapter);
        image = view.findViewById(R.id.camera);
        doc = view.findViewById(R.id.attachment);
        remove = view.findViewById(R.id.remove_file);
        fileLl = view.findViewById(R.id.file_ll);
        fileName = view.findViewById(R.id.filename);
        fileLl.setVisibility(View.GONE);
        send = view.findViewById(R.id.send);
        call = view.findViewById(R.id.call);
    }

    private void setListeners() {
        send.setOnClickListener(v -> {
            if (chatId.equals("")) {
                Toast.makeText(requireContext(), "Some error occurred", Toast.LENGTH_SHORT).show();
                return;
            }
            if (message.getText().toString().trim().equals("") && uri == null)
                return;
            Message m = new Message();
            m.setMessage(message.getText().toString().trim());
            m.setMedia(!(uri == null));
            m.setProgress(0);
            m.setTime(System.currentTimeMillis());
            m.setSender(FirebaseAuth.getInstance().getUid());
            m.setFileName(fileName.getText().toString());
            DocumentReference reference = FirebaseFirestore.getInstance()
                    .collection(Constants.CHATS)
                    .document(chatId)
                    .collection(Constants.MESSAGES)
                    .document();
            m.setMessageId(reference.getId());
            reference.set(m).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void unused) {
                    message.setText("");
                    fileName.setText("");
                    fileLl.setVisibility(View.GONE);
                    if (m.getMedia()) {
                        Map<String, Object> progressMap = new HashMap<>();
                        StorageReference storageReference = FirebaseStorage.getInstance()
                                .getReference()
                                .child(Constants.MESSAGES)
                                .child(chatId)
                                .child(m.getMessageId())
                                .child(m.getFileName());
                        storageReference.putFile(uri)
                                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                                        progressMap.put("progress", (snapshot.getBytesTransferred() * 100) / snapshot.getTotalByteCount());
                                        reference.update(progressMap);
                                    }
                                })
                                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                    @Override
                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                        storageReference.getDownloadUrl()
                                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                    @Override
                                                    public void onSuccess(Uri uri) {
                                                        progressMap.put("progress", 100L);
                                                        progressMap.put("url", uri.toString());
                                                        reference.update(progressMap);
                                                    }
                                                });
                                    }
                                });
                        uri = null;
                    }
                }
            });
        });
        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Image"), MEDIA_SELECT);
            }
        });
        doc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setType("*/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Document"), MEDIA_SELECT);
            }
        });
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uri = null;
                fileLl.setVisibility(View.GONE);
            }
        });
        fileName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if (uri == null) {
                    fileLl.setVisibility(View.GONE);
                    return;
                }
                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                startActivity(intent);*/
            }
        });
        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ACTION_DIAL, Uri.parse(String.format(Locale.ENGLISH, "tel:%s", bundle.getString(Constants.NUMBER))));
                startActivity(intent);
            }
        });
    }

    private void createChannelIdIfNotCreated() {
        if (FirebaseAuth.getInstance().getUid() == null)
            return;
        FirebaseFirestore.getInstance()
                .collection(Constants.USERS)
                .document(FirebaseAuth.getInstance().getUid())
                .collection(Constants.CHATS)
                .get()
                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                    @Override
                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                        for (DocumentSnapshot snapshot : queryDocumentSnapshots) {
                            if (snapshot.getId().equals(bundle.getString(Constants.UID))) {
                                chatId = snapshot.getString(Constants.CHAT_ID);
                                fetchChat();
                                return;
                            }
                        }
                        createId();
                    }

                    private void createId() {
                        chatId = FirebaseFirestore.getInstance()
                                .collection(Constants.CHATS)
                                .document().getId();
                        Map<String, Object> map = new HashMap<>();
                        map.put(Constants.CHAT_ID, chatId);
                        FirebaseFirestore.getInstance()
                                .collection(Constants.USERS)
                                .document(FirebaseAuth.getInstance().getUid())
                                .collection(Constants.CHATS)
                                .document(bundle.getString(Constants.UID))
                                .set(map);
                        FirebaseFirestore.getInstance()
                                .collection(Constants.USERS)
                                .document(bundle.getString(Constants.UID))
                                .collection(Constants.CHATS)
                                .document(FirebaseAuth.getInstance().getUid())
                                .set(map);
                    }
                });
        message.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                Map<String, Object> map = new HashMap<>();
                map.put(Constants.MESSAGE, requireContext().getString(R.string.online));
                if (s != null && !s.toString().trim().equals("")) {
//                    image.setVisibility(View.GONE);
                    map.put(Constants.MESSAGE, requireContext().getString(R.string.typing));
                }
                /*else {
                    image.setVisibility(View.VISIBLE);
                }*/
                FirebaseFirestore.getInstance()
                        .collection(Constants.ONLINE)
                        .document(FirebaseAuth.getInstance().getUid())
                        .update(map);
                lastUpdate = System.currentTimeMillis();
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void fetchChat() {
        FirebaseFirestore.getInstance()
                .collection(Constants.CHATS)
                .document(chatId)
                .collection(Constants.MESSAGES)
                .orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.ASCENDING)
                .addSnapshotListener(new EventListener<QuerySnapshot>() {
                    @Override
                    public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value == null)
                            return;
                        for (DocumentChange snapshot : value.getDocumentChanges()) {
                            if (snapshot.getType().equals(DocumentChange.Type.ADDED)) {
                                messages.add(snapshot.getDocument().toObject(Message.class));
                                adapter.notifyItemInserted(messages.size() - 1);
                                recyclerView.scrollToPosition(messages.size() - 1);
                            } else if (snapshot.getType().equals(DocumentChange.Type.MODIFIED)) {
                                Message m = snapshot.getDocument().toObject(Message.class);
                                for (int i = 0; i < messages.size(); ++i) {
                                    if (messages.get(i).getMessageId().equals(m.getMessageId())) {
                                        messages.add(i, m);
                                        messages.remove(i + 1);
                                        adapter.notifyItemChanged(i);
                                    }
                                }
                            }
                        }
                    }
                });
    }

    private void changeStatusBarColor(int color) {
        Window window = requireActivity().getWindow();
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(getResources().getColor(color, null));
    }

    private void setInfo() {
        Glide.with(requireContext()).load(bundle.getString(Constants.PROFILE_PIC_URL)).into(dp);
        name.setText(bundle.getString(Constants.NAME));
        FirebaseFirestore.getInstance()
                .collection(Constants.ONLINE)
                .document(bundle.getString(Constants.UID))
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                        if (value != null && value.exists() && value.contains(Constants.STATUS)) {
                            if (value.getBoolean(Constants.STATUS)) {
                                lastSeen.setText(value.getString(Constants.MESSAGE));
                            } else {
                                if (value.contains(Constants.TIME)) {
                                    Calendar calendar = Calendar.getInstance(TimeZone.getDefault());
                                    calendar.setTimeInMillis((Long) value.get(Constants.TIME));
                                    int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
                                    String month = calendar.getDisplayName(Calendar.MONTH, Calendar.SHORT_FORMAT, Locale.getDefault());
                                    int year = calendar.get(Calendar.YEAR);
                                    int hour = calendar.get(Calendar.HOUR_OF_DAY);
                                    int minute = calendar.get(Calendar.MINUTE);
                                    lastSeen.setText(String.format(Locale.getDefault(), "last seen %s %d %s %d", TimeFormatter.formatTime(hour, minute), dayOfMonth, month, year));
                                }
                            }
                        }
                    }
                });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == MEDIA_SELECT) {
                if (data == null || data.getData() == null)
                    return;
                uri = data.getData();
                Log.i("Asrik", data.getData().toString());
                FileMetaData metaData = FileDetails.getFileMetaData(requireContext(), uri);
                fileName.setText(metaData != null ? metaData.displayName : "file");
                fileLl.setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        requireActivity().findViewById(R.id.bottomAppBar).setVisibility(View.VISIBLE);
        changeStatusBarColor(R.color.white);
    }
}