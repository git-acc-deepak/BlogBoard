package com.deepak.android.blogboard;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class MainActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FloatingActionButton fab;
    private FirebaseFirestore db;
    private String currentUserId;
    private BottomNavigationView  bottomNavigationView;
    private HomeFragment homeFragment;
    private SearchFragment searchFragment;
    private NotificationsFragment notificationsFragment;
    private AccountFragment accountFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        // to load default fragment when the activity starts.
        changeFragments(new HomeFragment());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        final Toolbar mainToolbar = findViewById(R.id.main_toolbar);
        setSupportActionBar(mainToolbar);

        if (mAuth.getCurrentUser() != null) {
            bottomNavigationView = findViewById(R.id.bottom_nav);

            /**
             * Fragments initialization
             */

            homeFragment = new HomeFragment();
            notificationsFragment = new NotificationsFragment();
            searchFragment = new SearchFragment();
            accountFragment = new AccountFragment();

            /* changeFragments(homeFragment);*/
            bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {

                    switch (menuItem.getItemId()) {
                        case R.id.action_home:
                            changeFragments(homeFragment);
                            mainToolbar.setTitle("Blog Board");
                            getSupportFragmentManager().popBackStackImmediate();
                            return true;

                        case R.id.action_search:
                            mainToolbar.setTitle("Search");
                            changeFragments(searchFragment);
                            return true;

                        case R.id.action_notification:
                            mainToolbar.setTitle("Notifications");
                            changeFragments(notificationsFragment);
                            return true;

                        case R.id.bottom_action_account:
                            mainToolbar.setTitle("Account");
                            changeFragments(accountFragment);
                            return true;
                    }
                    return false;
                }
            });

            fab = findViewById(R.id.fab);
            fab.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent addPostIntent = new Intent(MainActivity.this, AddPostActivity.class);
                    startActivity(addPostIntent);
                }
            });
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        /**
         * checking if the user has already signed in else promote to login Activity.
         * else checks if the account is completely setup and promote user to settings page.
         */
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null){
         sendToLogin();
        }else {
            currentUserId = mAuth.getCurrentUser().getUid();
            db.collection("Users").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {

                    if (task.isSuccessful()){

                        if (!task.getResult().exists()){

                            Intent accountSettings = new Intent(MainActivity.this, AccountSetup.class);
                            startActivity(accountSettings);
                            finish();

                        }
                    }else {
                        String error = task.getException().getMessage();
                        Toast.makeText(MainActivity.this, "Error: "+ error, Toast.LENGTH_LONG).show();
                    }
                }
            });
        }
    }


    /**
     * creating a menu item for the app bar toolbar.
     * @param menu passing the operator and menu layout.
     * @return true if any item selected else return false.
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.main_menu, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()){
            case R.id.action_logout:
                logout();
                return true;
            case R.id.action_settings:
                Intent settings = new Intent(MainActivity.this,AccountSetup.class);
                startActivity(settings);
                return true;

                default:
                    return false;
        }

    }

    private void logout() {
        mAuth.signOut();
        sendToLogin();
    }

    private void sendToLogin() {
        Intent loginIntent = new Intent(MainActivity.this , LoginActivity.class);
        startActivity(loginIntent);
        finish();
    }

    /**
     * method changeFragments defines the transactions bw different fragments on home Page
     * like the home ,accounts, notification etc.
     * @param fragment bottom navigation fragments
     */
    private void changeFragments(Fragment fragment){
        FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container_view_tag, fragment);
        fragmentTransaction.commit();
    }
}
