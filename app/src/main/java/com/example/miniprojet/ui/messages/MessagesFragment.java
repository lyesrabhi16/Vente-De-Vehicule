package com.example.miniprojet.ui.messages;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.example.miniprojet.R;
import com.example.miniprojet.adapters.PersonItemAdapter;
import com.example.miniprojet.databinding.FragmentMessagesBinding;
import com.example.miniprojet.interfaces.RecyclerViewInterface;
import com.example.miniprojet.interfaces.RequestFinished;
import com.example.miniprojet.models.Message;
import com.example.miniprojet.models.MessagesViewModel;
import com.example.miniprojet.models.PersonItem;
import com.example.miniprojet.models.User;
import com.example.miniprojet.ui.messages.chat.ChatActivity;

import java.util.ArrayList;

public class MessagesFragment extends Fragment {

    private FragmentMessagesBinding binding;
    private ArrayList<PersonItem> chatsList;
    private PersonItemAdapter adapter;
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        MessagesViewModel messagesViewModel =
                new ViewModelProvider(this).get(MessagesViewModel.class);

        binding = FragmentMessagesBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        User user = User.getInstance(getContext());
        if (user.isLoggedin()){
            binding.progressBar.setVisibility(View.VISIBLE);
            chatsList = new ArrayList<PersonItem>();

            PersonItem i = new PersonItem();
            adapter = new PersonItemAdapter(chatsList, new RecyclerViewInterface() {
                @Override
                public void onItemClick(int position) {
                    Intent chat = new Intent(getContext() , ChatActivity.class);
                    PersonItem user = adapter.getList().get(position);
                    chat.putExtra("userID", user.getUserID());
                    chat.putExtra("AccountName", user.getTitle());
                    startActivity(chat);

                }
            });

            binding.recyclerChats.setAdapter(adapter);
            binding.progressBar.setVisibility(View.VISIBLE);
            Message.getChats(user.getID(), getContext(), new RequestFinished() {
                @Override
                public void onFinish(ArrayList args) {
                    if(binding == null) return;
                    binding.progressBar.setVisibility(View.GONE);
                    if(args.size()<1){
                        binding.recyclerChats.setVisibility(View.GONE);
                        binding.textMessages.setVisibility(View.VISIBLE);
                        binding.textMessages.setText(R.string.no_messages);
                    }
                    else{
                        if(binding == null) return;
                        adapter.setList(args);
                        binding.recyclerChats.setAdapter(adapter);
                        binding.recyclerChats.setVisibility(View.VISIBLE);
                    }
                }

                @Override
                public void onError(ArrayList args) {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.recyclerChats.setVisibility(View.GONE);
                    binding.textMessages.setVisibility(View.VISIBLE);
                    binding.textMessages.setText(R.string.refresh_request);
                }
            });


        }
        else{
            binding.progressBar.setVisibility(View.GONE);
            binding.recyclerChats.setVisibility(View.GONE);
            binding.textMessages.setVisibility(View.VISIBLE);
            binding.textMessages.setText("please login");
        }


        /*final TextView textView = binding.textMessages;
        messagesViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        */
        return root;

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}