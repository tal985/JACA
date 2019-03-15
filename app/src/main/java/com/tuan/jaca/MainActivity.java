package com.tuan.jaca;

import android.annotation.SuppressLint;
import android.app.FragmentTransaction;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.SparseArray;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;

//REMOVE
import static java.lang.System.out;

//Some code borrowed and modified from Professor Witchel

public class MainActivity extends AppCompatActivity
        implements FBLoginFrag.FBLoginInterface,
        FBCreateAccountFrag.FBCreateAccountInterface,
        NavigationView.OnNavigationItemSelectedListener
{
    protected Menu navDraw;
    protected List<ChampRec> CRList;
    protected ChampRec[] CRArr;
    protected boolean searchAlreadyOpened, titleChanged;
    protected Toolbar toolbar;
    protected MenuItem searchAction;
    protected SearchView searchbar;
    protected FirebaseAuth mAuth;
    protected ActionBarDrawerToggle abdToggle;
    protected CharSequence tempTitle = "";
    protected List<String> favChampKeys;
    protected FirebaseDatabase fbdb;
    protected FavListFrag flf;
    protected String version;
    protected static SparseArray<String> numsToSums;
    protected FBLoginFrag fblf = null;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initVars();
    }

    public void determineFavorites()
    {
        DatabaseReference userDBChamp;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        //Make sure that user isn't null; anon-users shouldn't even be able to get here anyways
        if(user != null)
        {
            userDBChamp = fbdb.getReference(user.getUid()).child("champions");
            userDBChamp.addListenerForSingleValueEvent
            (
                new ValueEventListener()
                {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                        //Add favorite champions to list
                        for(DataSnapshot ds: dataSnapshot.getChildren())
                        {
                            favChampKeys.add(ds.getKey());
                        }
                    }
                    @Override
                    public void onCancelled(DatabaseError databaseError)
                    {

                    }
                }
            );
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu)
    {
        searchAction = menu.findItem(R.id.action_search);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onBackPressed()
    {
        //If search is open, close it
        if(searchAlreadyOpened)
            searchClose();
        //On to home
        else if(getFragmentManager().getBackStackEntryCount() > 0)
        {
            FragFinish();
        }
        else if(getFragmentManager().getBackStackEntryCount() == 0)
            super.onBackPressed();
    }

    //Open fragment to the champion's post
    public void goToChamp(String champ, String title)
    {
        toolbar.setTitle(champ + ", " + title);
        toggleHamburgerToBack();
        ChampPostFrag cpf = ChampPostFrag.newInstance(findChampRecByName(champ));
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.main_frame, cpf);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    //Search for term
    protected void newSearch(String searchTerm)
    {
        ChampRec tempChamp = null;

        if(!searchTerm.isEmpty())
        {
            searchTerm = searchTerm.replace("'", "");
            tempChamp = findCRByKeyName(searchTerm);
        }
        if(tempChamp != null)
        {
            //Go to one post
            searchClose();
            FragFinish();
            goToChamp(tempChamp.name, tempChamp.title);
        }
        else
        {
            //Leave this toast in because keyboard would normally block the snackbar
            Toast.makeText(this, "Invalid Search!", Toast.LENGTH_LONG).show();
            Snackbar.make(findViewById(R.id.drawer_layout),
            "Invalid Search!", Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();

        switch(id)
        {
            case R.id.action_search:
                //Open search bar or if already open, do search
                if(!searchAlreadyOpened)
                    searchOpen();
                else
                {
                    ActionBar ab = getSupportActionBar();
                    searchbar = Objects.requireNonNull(ab).getCustomView().findViewById(R.id.searchBar);
                    newSearch(searchbar.getQuery().toString());
                }
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    //Open searchbar or search if already open
    public void searchOpen()
    {
        //Change action bar's title if necessary
        searchAlreadyOpened = true;
        ActionBar ab = getSupportActionBar();
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        if(toolbar.getTitle() != null && !toolbar.getTitle().toString().isEmpty())
        {
            tempTitle = toolbar.getTitle().toString();
            toolbar.setTitle("");
            titleChanged = true;
        }

        //Change hamburger to back arrow and listen for back button click
        abdToggle.setDrawerIndicatorEnabled(false);
        abdToggle.setHomeAsUpIndicator(Objects.requireNonNull(getDrawerToggleDelegate()).getThemeUpIndicator());
        abdToggle.setToolbarNavigationClickListener
        (
            new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    searchClose();
                }
            }
        );

        //Display searchbar
        Objects.requireNonNull(ab).setDisplayShowCustomEnabled(true);
        ab.setCustomView(R.layout.search_bar);
        searchbar = ab.getCustomView().findViewById(R.id.searchBar);
        searchbar.requestFocus();
        Objects.requireNonNull(imm).toggleSoftInput(InputMethodManager.SHOW_FORCED,0);

        //Inflate adapter
        ListView searchHints = findViewById(R.id.searchHints);
        searchHints.setVisibility(View.VISIBLE);
        findViewById(R.id.champGrid).setVisibility(View.INVISIBLE);
        String[] champNamesOnly = champNamesOnly();
        final ArrayAdapter<String> adapter = new ArrayAdapter<>
                (this, android.R.layout.simple_list_item_1, champNamesOnly);
        searchHints.setAdapter(adapter);

        //Listen for search from keyboard
        searchbar.setOnQueryTextListener
        (
            new SearchView.OnQueryTextListener()
            {
                @Override
                public boolean onQueryTextSubmit(String query)
                {
                    newSearch(query);
                    return true;
                }

                @Override
                public boolean onQueryTextChange(String newText)
                {
                    adapter.getFilter().filter(newText);
                    return false;
                }
            }
        );

        //Click on search hint's entries
        searchHints.setOnItemClickListener
        (
            new AdapterView.OnItemClickListener()
            {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int pos, long id)
                {
                    newSearch(parent.getItemAtPosition(pos).toString());
                }
            }
        );
    }

    //Get a String array of champion names only
    public String[] champNamesOnly()
    {
        String[] temp = new String[CRArr.length];
        for(int i = 0; i < temp.length; i++)
            temp[i] = CRArr[i].name;
        return temp;
    }

    //Close searchbar
    public void searchClose()
    {
        //Show champ grid again
        findViewById(R.id.searchHints).setVisibility(View.INVISIBLE);
        findViewById(R.id.champGrid).setVisibility(View.VISIBLE);

        //Change title back if necessary
        if(titleChanged)
        {
            toolbar.setTitle(tempTitle);
            tempTitle = "";
            titleChanged = false;
        }

        //If at home screen, switch back to hamburger
        if(getFragmentManager().getBackStackEntryCount() == 0)
            abdToggle.setDrawerIndicatorEnabled(true);
        else
        {

            abdToggle.setHomeAsUpIndicator(Objects.requireNonNull(getDrawerToggleDelegate()).getThemeUpIndicator());
            abdToggle.setToolbarNavigationClickListener
            (
                new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        FragFinish();
                    }
                }
            );
        }
        searchAlreadyOpened = false;
        ActionBar ab = getSupportActionBar();
        Objects.requireNonNull(ab).setDisplayShowCustomEnabled(false);
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        Objects.requireNonNull(imm).hideSoftInputFromWindow(Objects.requireNonNull(getCurrentFocus()).getWindowToken(), 0);
        searchbar.clearFocus();
    }
    //Finished FB logging in
    public void FBLoginFinish()
    {
        FragFinish();
    }

    //Dismiss a fragment
    public void FragFinish()
    {
        //Dismiss frag for anything past 1 frags
        if(getFragmentManager().getBackStackEntryCount() > 1)
        {
            if(fblf != null)
            {
                fblf.emailUsername.clearFocus();
                fblf.password.clearFocus();
                fblf = null;
            }
            //The favorites menu is the only place that can allow for a stack of more than 1
            toolbar.setTitle("Favorites");
            getFragmentManager().popBackStack();
        }
        //Go home
        else if(getFragmentManager().getBackStackEntryCount() == 1)
        {
            toolbar.setTitle("");
            getFragmentManager().popBackStack();
            abdToggle.setDrawerIndicatorEnabled(true);
            updateUserDisplay();
        }
    }

    //Go to create account fragment
    public void FBLoginToCreateAccount()
    {
        //Dismiss login fragment and create hamburger icon
        getFragmentManager().popBackStack();
        abdToggle.setDrawerIndicatorEnabled(true);
        toggleHamburgerToBack();

        //Create account fragment
        toolbar.setTitle("Create Account");
        FBCreateAccountFrag fcaf = FBCreateAccountFrag.newInstance();
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        ft.add(R.id.main_frame, fcaf);
        ft.addToBackStack(null);
        ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        ft.commit();
    }

    //Hamburger turned to back button
    protected void toggleHamburgerToBack()
    {
        abdToggle.setDrawerIndicatorEnabled(false);
        abdToggle.setHomeAsUpIndicator(Objects.requireNonNull(getDrawerToggleDelegate()).getThemeUpIndicator());
        abdToggle.setToolbarNavigationClickListener
        (
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FragFinish();
                    }
                }
        );
    }

    //Update navigation drawer information
    protected void updateUserDisplay()
    {
        String loginString;
        String userString;
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if(user != null)
        {
            userString = user.getDisplayName();
            loginString = "Logout";
            //Pull up favorites list if the user is logged in
            determineFavorites();
        }
        else
        {
            userString = "Not Logged In";
            loginString = "Login";
        }

        TextView drawerID = findViewById(R.id.drawerIDText);
        if(drawerID != null)
        {
            drawerID.setText(userString);
        }

        MenuItem logMenu = navDraw.findItem(R.id.nav_login);
        if(logMenu != null)
        {
            logMenu.setTitle(loginString);
            logMenu.setTitleCondensed(loginString);
        }
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item)
    {
        int id = item.getItemId();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);

        //Check which item was selected and function accordingly
        switch(id)
        {
            case R.id.nav_login:
                if(user != null)
                {
                    mAuth.signOut();
                }
                else
                {
                    //New firebase login fragment
                    toggleHamburgerToBack();
                    toolbar.setTitle("Login");
                    fblf = FBLoginFrag.newInstance();
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.add(R.id.main_frame, fblf);
                    ft.addToBackStack(null);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                }
                break;
            case R.id.nav_favorites:
                if(user != null)
                {
                    toggleHamburgerToBack();
                    toolbar.setTitle("Favorites");
                    FragmentTransaction ft = getFragmentManager().beginTransaction();
                    ft.add(R.id.main_frame, flf);
                    ft.addToBackStack(null);
                    ft.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
                    ft.commit();
                }
                else
                {
                    Snackbar.make(findViewById(R.id.drawer_layout),
                    "Please log in to access favorites!", Snackbar.LENGTH_LONG).show();

                }
                break;
            case R.id.nav_clear_cache:
                AsyncTask.execute
                (
                    new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            Glide.get(getApplicationContext()).clearDiskCache();
                        }
                    }
                );
                Glide.get(this).clearMemory();
                break;
        }

        return true;
    }

    //Create ChampRec Array
    public void createCRArr()
    {
        //Get LoL version which loads up the champion data and inflates the gridview
        try
        {
            getVersion();
        }
        catch (JSONException error)
        {
            error.printStackTrace();
        }

    }

    //Look up title based on key or checks if name is valid
    public String findTitleByKeyName(String keyOrName)
    {
        for(ChampRec cr: CRArr)
            if(keyOrName.equalsIgnoreCase(cr.key) || keyOrName.equalsIgnoreCase(cr.name))
                return cr.title;

        return null;
    }

    //Look up ID based on name or key. Case insensitive.
    public int findIDByNameKey(String input)
    {
        for(ChampRec cr: CRArr)
            if(input.equalsIgnoreCase(cr.name) || input.equalsIgnoreCase(cr.key))
                return cr.id;
        return -1;
    }

    //Look up key based on name
    public String findKeyByName(String name)
    {
        String tempKey = "";
        for(ChampRec i: CRList)
        {
            if(name.equalsIgnoreCase(i.name))
            {
                tempKey = i.key;
                break;
            }
        }
        return tempKey;
    }

    //Look up CR based on key or checks if name is valid
    public ChampRec findCRByKeyName(String keyOrName)
    {
        for(ChampRec cr: CRArr)
            if(keyOrName.equalsIgnoreCase(cr.key) || keyOrName.equalsIgnoreCase(cr.name))
                return cr;
        return null;
    }

    //Look up name based on key or checks if name is valid
    public String findNameByKeyName(String keyOrName)
    {
        for(ChampRec cr: CRArr)
            if(keyOrName.equalsIgnoreCase(cr.key) || keyOrName.equalsIgnoreCase(cr.name))
                return cr.name;
        return null;
    }

    //Look up name based on id
    public String findNameByID(int id)
    {

        for(ChampRec cr: CRArr)
            if(id == cr.id)
                return cr.name;
        return null;
    }

    //Return ChampRec from name
    public ChampRec findChampRecByName(String name)
    {
        ChampRec tempRec = new ChampRec();
        for(ChampRec cr: CRArr)
        {
            if(name.equalsIgnoreCase(cr.name))
            {
                tempRec = cr;
                break;
            }
        }
        return tempRec;
    }

    //Initialize variables
    @SuppressLint("ResourceType")
    public void initVars()
    {
        favChampKeys = new ArrayList<>();
        searchAlreadyOpened = false;
        titleChanged = false;
        flf = FavListFrag.newInstance();

        //Set toolbar
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("");
        toolbar.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                titleToast();
            }
        });
        setSupportActionBar(toolbar);
        toolbar.setOverflowIcon
                (ContextCompat.getDrawable(getApplicationContext(), R.drawable.ic_settings_black_24dp));
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        //Set navigation view for navigation drawer
        NavigationView navView = findViewById(R.id.nav_view);
        navView.setNavigationItemSelectedListener(this);
        navDraw = navView.getMenu();

        //Create drawer button on action bar
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        abdToggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        {
            @Override
            public void onDrawerOpened(View dView)
            {
                super.onDrawerOpened(dView);
                updateUserDisplay();
            }
        };
        drawer.addDrawerListener(abdToggle);
        abdToggle.syncState();

        //Create mapping of summoner IDs to summoner names
        numsToSums = new SparseArray<>();
        numsToSums.put(1, "Boost");
        numsToSums.put(3, "Exhaust");
        numsToSums.put(4, "Flash");
        numsToSums.put(6, "Haste");
        numsToSums.put(7, "Heal");
        numsToSums.put(11, "Smite");
        numsToSums.put(12, "Teleport");
        numsToSums.put(14, "Dot");
        numsToSums.put(21, "Barrier");

        //Initialize firebase
        FirebaseApp.initializeApp(this);
        mAuth = FirebaseAuth.getInstance();
        fbdb = FirebaseDatabase.getInstance();
        updateUserDisplay();

        //Create champions record array
        createCRArr();
    }

    //Create toast of the champion's name and title, in case the toolbar truncates
    public void titleToast()
    {
        if(!toolbar.getTitle().equals(""))
            Toast.makeText(this, toolbar.getTitle(), Toast.LENGTH_LONG).show();
    }

    //Get LoL version
    public void getVersion() throws JSONException
    {
        //https://ddragon.leagueoflegends.com/realms/na.json
        String url = "https://ddragon.leagueoflegends.com/realms/na.json";
        JsonObjectRequest jor = new JsonObjectRequest
        (
            Request.Method.GET, url, null, new Response.Listener<JSONObject>()
            {

                @Override
                public void onResponse(JSONObject response)
                {
                    try
                    {
                        version = response.getJSONObject("n").get("item").toString();
                        getChampData();
                    }
                    catch(JSONException error)
                    {
                        error.printStackTrace();
                    }

                }
            },
            new Response.ErrorListener()
            {

                @Override
                public void onErrorResponse(VolleyError error)
                {
                    error.printStackTrace();
                }
            }
        )
        {
            //User Agent
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "CS371M-tal985-JACA");
                return headers;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(jor);
    }

    //Retrieve champions data
    public void getChampData() throws JSONException
    {
        String url = "https://ddragon.leagueoflegends.com/cdn/" + version + "/data/en_US/champion.json";

        JsonObjectRequest jor = new JsonObjectRequest
        (
            Request.Method.GET, url, null, new Response.Listener<JSONObject>()
            {
                @Override
                public void onResponse(JSONObject response)
                {
                    //TODO: update champion data

                    try
                    {
                        //this list should be in order thanks to Rito
                        List<ChampRec> tempCRList = new ArrayList<>();
                        String tempKey;
                        ChampRec myCR;
                        JSONObject champ;
                        JSONObject data = response.getJSONObject("data");
                        Iterator<String> champs = data.keys();
                        while(champs.hasNext())
                        {
                            myCR = new ChampRec();
                            tempKey = champs.next();
                            champ = data.getJSONObject(tempKey);
                            myCR.name = champ.getString("name");
                            //I know, the key and id are backwards
                            myCR.id = champ.getInt("key");
                            myCR.key = champ.getString("id");
                            myCR.title = champ.getString("title");
                            tempCRList.add(myCR);
                        }
                        CRArr = new ChampRec[tempCRList.size()];
                        tempCRList.toArray(CRArr);
                        inflateGridview();
                    }
                    catch (JSONException e)
                    {
                        out.println("ERROR. WTF.");
                        e.printStackTrace();
                    }
                }
            },
                new Response.ErrorListener()
                {

                    @Override
                    public void onErrorResponse(VolleyError error)
                    {
                        error.printStackTrace();
                    }
                }
        )
        {
            //User Agent
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String> headers = new HashMap<>();
                headers.put("User-Agent", "CS371M-tal985-JACA");
                return headers;
            }
        };

        RequestQueue rq = Volley.newRequestQueue(this);
        rq.add(jor);
    }

    //Inflate gridview of the champions
    public void inflateGridview()
    {
        GridView gv = findViewById(R.id.champGrid);
        final ProfileAdapter pa = new ProfileAdapter(this);
        gv.setAdapter(pa);
        gv.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            //On click functionality
            public void onItemClick(AdapterView<?> adapterView, View view, int pos, long id)
            {
                //Go to one post fragment
                goToChamp(CRArr[pos].name, CRArr[pos].title);
            }
        });
    }
}
