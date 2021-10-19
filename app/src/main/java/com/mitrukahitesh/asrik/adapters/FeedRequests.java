package com.mitrukahitesh.asrik.adapters;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.mitrukahitesh.asrik.R;
import com.mitrukahitesh.asrik.models.BloodRequest;
import com.mitrukahitesh.asrik.utility.Constants;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

public class FeedRequests extends RecyclerView.Adapter<FeedRequests.CustomVH> {

    private Context context;
    private final List<BloodRequest> requests = new ArrayList<>();
    private Long last = System.currentTimeMillis();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final Set<Integer> updatedAt = new HashSet<>();
    private final Set<String> gotOnlineStatus = new HashSet<>();

    public FeedRequests(Context context) {
        this.context = context;
        fetchData();
    }

    private void fetchData() {
        CollectionReference reference = db.collection(Constants.REQUESTS);
        Query query = reference.
                whereEqualTo(Constants.VERIFIED.toLowerCase(Locale.ROOT), true).
                orderBy(Constants.TIME.toLowerCase(Locale.ROOT), Query.Direction.DESCENDING).
                whereLessThan(Constants.TIME.toLowerCase(Locale.ROOT), last).
                limit(15);
        query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    if (task.getResult() == null)
                        return;
                    for (QueryDocumentSnapshot snapshot : task.getResult()) {
                        requests.add(snapshot.toObject(BloodRequest.class));
                        notifyItemInserted(requests.size() - 1);
                    }
                    if (!task.getResult().isEmpty())
                        last = requests.get(requests.size() - 1).getTime();
                    Log.i("Asrik", "fetched " + task.getResult().size() + " " + last);
                } else {
                    Log.i("Asrik", "get failed with ", task.getException());
                }
            }
        });
    }

    @NonNull
    @Override
    public CustomVH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new CustomVH(LayoutInflater.from(parent.getContext()).inflate(R.layout.view_request, parent, false));
    }

    @Override
    public void onBindViewHolder(@NonNull CustomVH holder, int position) {
        holder.setView(requests.get(position), position);
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    public class CustomVH extends RecyclerView.ViewHolder {

        private final LinearLayout locate, share, chat, on, off, detailsHolder;
        private final TextView title, name, units, address, severity;
        private final CircleImageView dp;

        public CustomVH(@NonNull View itemView) {
            super(itemView);
            detailsHolder = itemView.findViewById(R.id.details_holder);
            locate = itemView.findViewById(R.id.location);
            share = itemView.findViewById(R.id.share);
            chat = itemView.findViewById(R.id.chat);
            on = itemView.findViewById(R.id.online);
            off = itemView.findViewById(R.id.offline);
            title = itemView.findViewById(R.id.title);
            name = itemView.findViewById(R.id.name);
            units = itemView.findViewById(R.id.units);
            address = itemView.findViewById(R.id.address);
            severity = itemView.findViewById(R.id.severity);
            dp = itemView.findViewById(R.id.dp);
            setListeners();
        }

        private void setListeners() {
            detailsHolder.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setDataAndType(Uri.parse(requests.get(getAdapterPosition()).getDocumentUrl()), "application/pdf");
                    context.startActivity(intent);
                }
            });
        }

        public void setView(BloodRequest request, int position) {
            if ((position + 1) % 15 == 0 && !updatedAt.contains(position)) {
                fetchData();
                updatedAt.add(position);
            }
            if (request.getProfilePicUrl() == null || request.getProfilePicUrl().equals("")) {
                Glide.with(context).load(AppCompatResources.getDrawable(context, R.drawable.ic_usercircle)).into(dp);
            } else {
                Glide.with(context).load(request.getProfilePicUrl()).into(dp);
            }
            title.setText(String.format("%s in %s", request.getBloodGroup(), request.getCity()));
            units.setText(String.format(Locale.getDefault(), "%d units", request.getUnits()));
            name.setText(request.getName());
            address.setText(request.getAddress());
            severity.setText(request.getSeverity());
            if (request.isUserOnline()) {
                on.setVisibility(View.VISIBLE);
                off.setVisibility(View.GONE);
            } else {
                on.setVisibility(View.GONE);
                off.setVisibility(View.VISIBLE);
            }
            if (gotOnlineStatus.contains(request.getRequestId()))
                return;
            FirebaseFirestore.getInstance()
                    .collection(Constants.ONLINE)
                    .document(request.getUid())
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            if (task.isSuccessful() && task.getResult() != null && task.getResult().contains(Constants.STATUS)) {
                                request.setUserOnline((Boolean) task.getResult().get(Constants.STATUS));
                                notifyItemChanged(position);
                            }
                        }
                    });
            gotOnlineStatus.add(request.getRequestId());
        }
    }
}