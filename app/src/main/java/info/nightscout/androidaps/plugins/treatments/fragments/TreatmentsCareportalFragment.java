package info.nightscout.androidaps.plugins.treatments.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.otto.Subscribe;

import java.util.List;

import info.nightscout.androidaps.MainApp;
import info.nightscout.androidaps.R;
import info.nightscout.androidaps.database.BlockingAppRepository;
import info.nightscout.androidaps.database.transactions.InvalidateTherapyEventTransaction;
import info.nightscout.androidaps.db.CareportalEvent;
import info.nightscout.androidaps.events.EventCareportalEventChange;
import info.nightscout.androidaps.plugins.common.SubscriberFragment;
import info.nightscout.androidaps.plugins.general.nsclient.NSUpload;
import info.nightscout.androidaps.plugins.general.nsclient.UploadQueue;
import info.nightscout.androidaps.plugins.general.nsclient.events.EventNSClientRestart;
import info.nightscout.androidaps.utils.DateUtil;
import info.nightscout.androidaps.utils.SP;
import info.nightscout.androidaps.utils.Translator;

/**
 * Created by mike on 13/01/17.
 */

public class TreatmentsCareportalFragment extends SubscriberFragment implements View.OnClickListener {

    RecyclerView recyclerView;
    LinearLayoutManager llm;
    Button refreshFromNS;

    Context context;

    public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.CareportalEventsViewHolder> {

        List<CareportalEvent> careportalEventList;

        RecyclerViewAdapter(List<CareportalEvent> careportalEventList) {
            this.careportalEventList = careportalEventList;
        }

        @Override
        public CareportalEventsViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.treatments_careportal_item, viewGroup, false);
            CareportalEventsViewHolder CareportalEventsViewHolder = new CareportalEventsViewHolder(v);
            return CareportalEventsViewHolder;
        }

        @Override
        public void onBindViewHolder(CareportalEventsViewHolder holder, int position) {
            CareportalEvent careportalEvent = careportalEventList.get(position);
            holder.ns.setVisibility(careportalEvent.backing.getInterfaceIDs().getNightscoutId() != null ? View.VISIBLE : View.GONE);
            holder.date.setText(DateUtil.dateAndTimeString(careportalEvent.date));
            holder.note.setText(careportalEvent.getNotes());
            holder.type.setText(Translator.translate(careportalEvent.eventType));
            holder.remove.setTag(careportalEvent);
        }

        @Override
        public int getItemCount() {
            return careportalEventList.size();
        }

        @Override
        public void onAttachedToRecyclerView(RecyclerView recyclerView) {
            super.onAttachedToRecyclerView(recyclerView);
        }

        public class CareportalEventsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
            CardView cv;
            TextView date;
            TextView type;
            TextView note;
            TextView remove;
            TextView ns;

            CareportalEventsViewHolder(View itemView) {
                super(itemView);
                cv = (CardView) itemView.findViewById(R.id.careportal_cardview);
                date = (TextView) itemView.findViewById(R.id.careportal_date);
                type = (TextView) itemView.findViewById(R.id.careportal_type);
                note = (TextView) itemView.findViewById(R.id.careportal_note);
                ns = (TextView) itemView.findViewById(R.id.ns_sign);
                remove = (TextView) itemView.findViewById(R.id.careportal_remove);
                remove.setOnClickListener(this);
                remove.setPaintFlags(remove.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);
            }

            @Override
            public void onClick(View v) {
                final CareportalEvent careportalEvent = (CareportalEvent) v.getTag();
                switch (v.getId()) {
                    case R.id.careportal_remove:
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        builder.setTitle(MainApp.gs(R.string.confirmation));
                        builder.setMessage(MainApp.gs(R.string.removerecord) + "\n" + DateUtil.dateAndTimeString(careportalEvent.date));
                        builder.setPositiveButton(MainApp.gs(R.string.ok), (dialog, id) -> {
                            BlockingAppRepository.INSTANCE.runTransaction(new InvalidateTherapyEventTransaction(careportalEvent.backing.getId()));
                        });
                        builder.setNegativeButton(MainApp.gs(R.string.cancel), null);
                        builder.show();
                        break;
                }
            }
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.treatments_careportal_fragment, container, false);

        recyclerView = (RecyclerView) view.findViewById(R.id.careportal_recyclerview);
        recyclerView.setHasFixedSize(true);
        llm = new LinearLayoutManager(view.getContext());
        recyclerView.setLayoutManager(llm);

        RecyclerViewAdapter adapter = new RecyclerViewAdapter(MainApp.getDbHelper().getCareportalEvents(false));
        recyclerView.setAdapter(adapter);

        refreshFromNS = (Button) view.findViewById(R.id.careportal_refreshfromnightscout);
        refreshFromNS.setOnClickListener(this);

        view.findViewById(R.id.careportal_removeandroidapsstartedevents).setOnClickListener(this);

        context = getContext();

        boolean nsUploadOnly = SP.getBoolean(R.string.key_ns_upload_only, false);
        if (nsUploadOnly)
            refreshFromNS.setVisibility(View.GONE);

        updateGUI();
        return view;
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.careportal_refreshfromnightscout:
                AlertDialog.Builder builder = new AlertDialog.Builder(this.getContext());
                builder.setTitle(MainApp.gs(R.string.confirmation));
                builder.setMessage(MainApp.gs(R.string.refresheventsfromnightscout) + " ?");
                builder.setPositiveButton(MainApp.gs(R.string.ok), new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        //MainApp.getDbHelper().resetCareportalEvents();
                        MainApp.bus().post(new EventNSClientRestart());
                    }
                });
                builder.setNegativeButton(MainApp.gs(R.string.cancel), null);
                builder.show();
                break;
            case R.id.careportal_removeandroidapsstartedevents:
                builder = new AlertDialog.Builder(context);
                builder.setTitle(MainApp.gs(R.string.confirmation));
                builder.setMessage(MainApp.gs(R.string.careportal_removestartedevents));
                builder.setPositiveButton(MainApp.gs(R.string.ok), (dialog, id) -> {
                    removeAndroidAPSStatedEvents();
                });
                builder.setNegativeButton(MainApp.gs(R.string.cancel), null);
                builder.show();
                break;
        }

    }

    @Subscribe
    public void onStatusEvent(final EventCareportalEventChange ev) {
        updateGUI();
    }

    @Override
    protected void updateGUI() {
        Activity activity = getActivity();
        if (activity != null)
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    recyclerView.swapAdapter(new RecyclerViewAdapter(MainApp.getDbHelper().getCareportalEvents(false)), false);
                }
            });
    }

    private void removeAndroidAPSStatedEvents() {
        List<CareportalEvent> events = MainApp.getDbHelper().getCareportalEvents(false);
        for (int i = 0; i < events.size(); i++) {
            CareportalEvent careportalEvent = events.get(i);
            if (careportalEvent.json.contains(MainApp.gs(R.string.androidaps_start))) {
                final String _id = careportalEvent._id;
                if (NSUpload.isIdValid(_id)) {
                    NSUpload.removeCareportalEntryFromNS(_id);
                } else {
                    UploadQueue.removeID("dbAdd", _id);
                }
                //MainApp.getDbHelper().delete(careportalEvent);
            }
        }
    }
}
