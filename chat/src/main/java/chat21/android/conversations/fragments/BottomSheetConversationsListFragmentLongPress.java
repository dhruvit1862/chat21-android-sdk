package chat21.android.conversations.fragments;

import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.BottomSheetDialogFragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;

import chat21.android.R;
import chat21.android.conversations.models.Conversation;
import chat21.android.core.ChatManager;
import chat21.android.dao.node.NodeDAO;
import chat21.android.dao.node.NodeDAOImpl;
import chat21.android.user.models.IChatUser;

/**
 * Created by stefanodp91 on 28/09/17.
 */
public class BottomSheetConversationsListFragmentLongPress extends BottomSheetDialogFragment implements
        View.OnClickListener {

    private static final String DEBUG_TAG = BottomSheetConversationsListFragmentLongPress.class.getName();

    private static final String _BOTTOM_SHEET_CONVERSATIONS_LIST_FRAGMENT_LONG_PRESS_EXTRAS_CONVERSATION =
            "_BOTTOM_SHEET_CONVERSATIONS_LIST_FRAGMENT_LONG_PRESS_EXTRAS_CONVERSATION";

    private Conversation mConversation;
    private IChatUser mLoggedUser;

    private Button mDeleteConversationView;

    private NodeDAO mNodeDAO;

    public static BottomSheetConversationsListFragmentLongPress
    newInstance(Conversation conversation) {
        BottomSheetConversationsListFragmentLongPress f =
                new BottomSheetConversationsListFragmentLongPress();
        Bundle args = new Bundle();
        args.putSerializable(_BOTTOM_SHEET_CONVERSATIONS_LIST_FRAGMENT_LONG_PRESS_EXTRAS_CONVERSATION, conversation);
        f.setArguments(args);
        return f;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mConversation = (Conversation) getArguments()
                .getSerializable(_BOTTOM_SHEET_CONVERSATIONS_LIST_FRAGMENT_LONG_PRESS_EXTRAS_CONVERSATION);
        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.onCreate: " +
                "mConversation == " + mConversation.toString());

        mLoggedUser = ChatManager.getInstance().getLoggedUser();
        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.onCreate:" +
                " mLoggedUser == " + mLoggedUser.toString());

        mNodeDAO = new NodeDAOImpl(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater
                .inflate(R.layout.fragment_bottom_sheet_conversation_list_long_press,
                        container, false);

        registerViews(rootView);
        initViews();
        initListeners();

        return rootView;
    }


    private void registerViews(View rootView) {
        mDeleteConversationView = rootView.findViewById(R.id.btn_delete_conversation);
    }

    private void initViews() {

    }

    private void initListeners() {
        mDeleteConversationView.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.btn_delete_conversation) {
            onDeleteConversationActionListener();
        }
    }

    private void onDeleteConversationActionListener() {
        mLoggedUser = ChatManager.getInstance().getLoggedUser();
        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.onDeleteConversationActionListener");

        showRemoveMemberAlertDialog();
    }

    private void showRemoveMemberAlertDialog() {
        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.showRemoveMemberAlertDialog");

        new AlertDialog.Builder(getActivity())
                .setTitle(getString(R.string.fragment_bottom_sheet_conversation_list_confirm_delete_conversation_alert_title))
                .setMessage(getString(R.string.fragment_bottom_sheet_conversation_list_confirm_delete_conversation_alert_message))
                .setPositiveButton(getString(R.string.fragment_bottom_sheet_conversation_list_confirm_delete_conversation_alert_positive_button_label),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                                        ".showRemoveMemberAlertDialog.setPositiveButton");

                                perfomDeleteConversation();
                            }
                        })
                .setNegativeButton(getString(R.string.fragment_bottom_sheet_conversation_list_confirm_delete_conversation_alert_positive_button_negative),
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                                        ".showRemoveMemberAlertDialog.setNegativeButton");

                                // dismiss the dialog
                                dialogInterface.dismiss();

                                // dismiss the bottomsheet
                                getDialog().dismiss();
                            }
                        }).show();
    }

    private void perfomDeleteConversation() {
        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress.perfomDeleteConversation");

//        String conversationId;
//        if (StringUtils.isValid(mConversation.getGroup_id())) {
//            conversationId = mConversation.getGroup_id();
//        } else {
//            conversationId = ConversationUtils.getConversationId(
//                    mConversation.getSender(), mConversation.getRecipient());
//        }

        String conversationId = mConversation.getConversationId();


        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                ".perfomDeleteConversation: conversationId == " + conversationId);

        DatabaseReference nodeConversation = mNodeDAO.getNodeConversations(mLoggedUser.getId())
                .child(conversationId);
        Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                ".perfomDeleteConversation: nodeConversation == " + nodeConversation.toString());

        nodeConversation.removeValue(onConversationRemoved);
    }

    private DatabaseReference.CompletionListener onConversationRemoved
            = new DatabaseReference.CompletionListener() {
        @Override
        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

            if (databaseError == null) {
                // no errors
                Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                        ".onConversationRemoved: no errors");

                // dismiss the bottomsheet
                getDialog().dismiss();
            } else {
                // there are error
                Log.d(DEBUG_TAG, "BottomSheetConversationsListFragmentLongPress" +
                        ".onConversationRemoved: " + databaseError.toString());

                Toast.makeText(getActivity(), "", Toast.LENGTH_SHORT).show();
            }
        }
    };
}