package com.example.swapify.chats;

import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;

import com.example.swapify.R;

import java.util.Objects;

public class SearchFragment extends Fragment {
    public interface OnSearchInputListener {
        void onSearchInput(String searchText);
    }

    private OnSearchInputListener onSearchInputListener;
    private EditText searchEditText;
    private boolean requestFocus;
    public SearchFragment() {
        // Required empty public constructor
        requestFocus = false;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            onSearchInputListener = (OnSearchInputListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnSearchInputListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        onSearchInputListener = null;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);
        searchEditText = view.findViewById(R.id.searchEditText);
        if (requestFocus) {
            openKeyboardAndFocusSearch();
            requestFocus = false;
        }
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                if (onSearchInputListener != null) {
                    onSearchInputListener.onSearchInput(s.toString());
                }
            }
        });

        return view;
    }

    public void openKeyboardAndFocusSearch() {
        if (searchEditText != null) {
            searchEditText.requestFocus();
            InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);
        } else {
            requestFocus = true;
        }
    }

    public void closeFragment() {
        requireActivity().getSupportFragmentManager().beginTransaction().remove(this).commit();
    }
}