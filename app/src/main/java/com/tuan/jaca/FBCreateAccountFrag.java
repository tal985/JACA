package com.tuan.jaca;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.UserProfileChangeRequest;

//Code modified and borrowed from Professor Witchel

public class FBCreateAccountFrag extends Fragment
{
    protected FBCreateAccountFrag.FBCreateAccountInterface fbcai;
    protected FirebaseAuth mAuth;
    protected View rootView;
    protected Button createButton;

    public interface FBCreateAccountInterface
    {
        void FBLoginFinish();
    }

    public static FBCreateAccountFrag newInstance()
    {
        return new FBCreateAccountFrag();
    }

    @Override
    public void onAttach(Context c)
    {
        super.onAttach(c);

        try
        {
            fbcai = (FBCreateAccountInterface) c;
        }
        catch(ClassCastException e)
        {
            throw new ClassCastException(c.toString() + " must implement FBCreateAccountInterface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater layoutInflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(rootView == null)
            rootView = layoutInflater.inflate(R.layout.create_account, container, false);
        createButton = (Button) rootView.findViewById(R.id.createButton);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        final EditText emailUsername = (EditText) rootView.findViewById(R.id.loginUser);
        final EditText password = (EditText) rootView.findViewById(R.id.loginPass);
        final EditText passwordAgain = (EditText) rootView.findViewById(R.id.passwordAgain);
        mAuth = FirebaseAuth.getInstance();

        //Tab to switch focus to next field
        emailUsername.setOnKeyListener
        (
            new View.OnKeyListener()
            {
                @Override

                public boolean onKey(View v, int keyCode, KeyEvent event)
                {
                    if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_TAB)
                    {
                        password.setFocusableInTouchMode(true);
                        password.requestFocus();
                        return true;
                    }
                    return false;
                }
            }
        );

        password.setOnKeyListener
        (
            new View.OnKeyListener()
            {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event)
                {
                    if(event.getAction() == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_TAB)
                    {
                        passwordAgain.setFocusableInTouchMode(true);
                        passwordAgain.requestFocus();
                        return true;
                    }
                    return false;
                }
            }
        );

        //Button functionality
        createButton.setOnClickListener
        (
            new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    final String username = emailUsername.getText().toString().trim();
                    String pass = password.getText().toString().trim();
                    String pass2 = passwordAgain.getText().toString().trim();

                    //Validate the creation of login data
                    String validateString = EmailPassValid.validate(getResources(), username, pass, pass2);
                    if(!validateString.isEmpty())
                    {
                        Snackbar.make(rootView, validateString, Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    //Create progress bar and prevent user input
                    final ProgressBar pb = rootView.findViewById(R.id.loadingBar);
                    pb.setVisibility(View.VISIBLE);
                    ((Activity) getContext())
                        .getWindow()
                        .setFlags
                        (
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        );

                    //Set up new FB user
                    mAuth.createUserWithEmailAndPassword(username, pass)
                        .addOnCompleteListener
                        (
                            getActivity(), new OnCompleteListener<AuthResult>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task)
                                {
                                    //Re-enable user input
                                    pb.setVisibility(View.GONE);
                                    ((Activity) getContext())
                                        .getWindow()
                                        .clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE);

                                    //Check sign in status
                                    if(task.isSuccessful())
                                    {
                                        UserProfileChangeRequest profileUpdates =
                                            new UserProfileChangeRequest.Builder()
                                            .setDisplayName(username).build();
                                        mAuth.getCurrentUser().updateProfile(profileUpdates);
                                        fbcai.FBLoginFinish();
                                    }
                                    else
                                    {
                                        Snackbar.make(rootView, "Authentication failed!",
                                        Snackbar.LENGTH_LONG).setAction("Action", null)
                                        .show();
                                    }
                                }
                            }
                        );
                }
            }
        );
    }
}
