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
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.Objects;

//Code modified and borrowed from Professor Witchel

public class FBLoginFrag extends Fragment
{
    protected FBLoginInterface fbli;
    protected FirebaseAuth mAuth;
    protected Button login;
    protected Button createFrag;
    protected View rootView;
    protected EditText emailUsername, password;

    public interface FBLoginInterface
    {
        void FBLoginFinish();
        void FBLoginToCreateAccount();
    }

    public static FBLoginFrag newInstance()
    {
        return new FBLoginFrag();
    }

    @Override
    public void onAttach(Context c)
    {
        super.onAttach(c);

        try
        {
            fbli = (FBLoginInterface) c;
        }
        catch(ClassCastException e)
        {
            throw new ClassCastException(c.toString() + " must implement FBEmailPassInterface");
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        if(rootView == null)
            rootView = inflater.inflate(R.layout.login, container, false);
        login = rootView.findViewById(R.id.loginButton);
        createFrag = rootView.findViewById(R.id.toCreate);
        return rootView;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        emailUsername = rootView.findViewById(R.id.loginUser);
        password = rootView.findViewById(R.id.loginPass);
        mAuth = FirebaseAuth.getInstance();

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

        //Create account button
        createFrag.setOnClickListener
        (
            new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    fbli.FBLoginToCreateAccount();
                }
            }
        );

        //Login button
        login.setOnClickListener
        (
            new View.OnClickListener()
            {
                public void onClick(View view)
                {
                    String username = emailUsername.getText().toString().trim();
                    String pass = password.getText().toString().trim();

                    //Validate login data
                    String validateString = EmailPassValid.validate(getResources(), username, pass, null);
                    if(!validateString.isEmpty())
                    {
                        Snackbar.make(rootView, validateString, Snackbar.LENGTH_LONG).show();
                        return;
                    }

                    //Create progress bar and prevent user input
                    final ProgressBar pb = rootView.findViewById(R.id.loadingLoginBar);
                    pb.setVisibility(View.VISIBLE);
                    ((Activity) getContext())
                        .getWindow()
                        .setFlags
                        (
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                        );

                    //Login
                    mAuth.signInWithEmailAndPassword(username, pass)
                        .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>()
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
                                    fbli.FBLoginFinish();
                                }
                                else
                                    Snackbar.make(rootView, "Authentication failed: " +
                                        Objects.requireNonNull(task.getException()).getMessage(), Snackbar.LENGTH_LONG)
                                        .setAction("Action", null).show();
                            }
                        });
                }
            }
        );
    }
    //TODO: close keyboard if back is hit
}
