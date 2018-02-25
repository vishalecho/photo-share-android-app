package io.github.imcgeek.photosharing;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.Manifest;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.github.imcgeek.photosharing.dataModels.Groups;
import io.github.imcgeek.photosharing.dataModels.Photos;
import io.github.imcgeek.photosharing.fragment.HomeFragment;
import io.github.imcgeek.photosharing.fragment.PhotosFragment;
import io.github.imcgeek.photosharing.utils.CircleTransform;

public class HomeActivity extends AppCompatActivity{
    private static final String TAG = HomeActivity.class.getSimpleName() ;
    private NavigationView navigationView;
    private DrawerLayout drawer;
    private View navHeader;
    private ImageView imgNavHeaderBg, imgProfile;
    private TextView txtName, txtEmail;
    private Toolbar toolbar;
    private Boolean isFabOpen = false;
    private FloatingActionButton fab,fabCreateGroup,fabCapturePhoto;
    private Animation fab_open,fab_close,rotate_forward,rotate_backward;

    // index to identify current nav menu item
    public static int navItemIndex = 0;

    // tags used to attach the fragments
    private static final String TAG_HOME = "home";
    private static final String TAG_PHOTOS = "photos";
    public static String CURRENT_TAG = TAG_HOME;

    // toolbar titles respected to selected nav menu item
    private String[] activityTitles;

    // flag to load home fragment when user presses back key
    private boolean shouldLoadHomeFragOnBackPress = true;
    private Handler mHandler;

    AlertDialog dialog;

    FirebaseAuth mAuth;
    FirebaseUser user;
    private DatabaseReference mFirebaseDatabase;
    private FirebaseDatabase mFirebaseInstance;

    private DatabaseReference mFirebaseDatabasePhoto;

    private FirebaseStorage mFirebaseStoragePhoto;
    private StorageReference mFirebaseStorage;

    public  static final int RequestPermissionCode  = 1 ;
    public static final int MY_PERMISSIONS_REQUEST_CAMERA = 1;
    static final int REQUEST_IMAGE_CAPTURE = 1;
    Intent takePictureIntent ;
    ImageView imageView;
    private volatile String photoPath;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        imageView = (ImageView) findViewById(R.id.imageView);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        /*Firebase database*/
        mFirebaseInstance = FirebaseDatabase.getInstance();
        // get reference to 'groups' node
        mFirebaseDatabase = mFirebaseInstance.getReference("groups");

        mFirebaseDatabasePhoto = mFirebaseInstance.getReference("photos");
        // store app title to 'app_title' node
        mFirebaseInstance.getReference("app_title").setValue("Photo Sharing");
        // app_title change listener
        mFirebaseInstance.getReference("app_title").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Log.e(TAG, "App title updated");

                String appTitle = dataSnapshot.getValue(String.class);

                // update toolbar title
                getSupportActionBar().setTitle(appTitle);
            }

            @Override
            public void onCancelled(DatabaseError error) {
                // Failed to read value
                Log.e(TAG, "Failed to read app title value.", error.toException());
            }
        });
        mFirebaseStoragePhoto = FirebaseStorage.getInstance();
        mFirebaseStorage = mFirebaseStoragePhoto.getReference();
        mFirebaseStorage = mFirebaseStorage.child("photos");
        mFirebaseStorage = mFirebaseStorage.child(user.getUid());

        mHandler = new Handler();
        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        navigationView = (NavigationView) findViewById(R.id.nav_view);
        fab = (FloatingActionButton) findViewById(R.id.fab);
        fabCreateGroup = (FloatingActionButton) findViewById(R.id.fabCreateGroup);
        fabCapturePhoto = (FloatingActionButton) findViewById(R.id.fabCapturePhoto);
        fab_open = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fab_open);
        fab_close = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.fab_close);
        rotate_forward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_forward);
        rotate_backward = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.rotate_backward);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                animateFAB();
            }
        });
        fabCreateGroup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder mBuilder = new AlertDialog.Builder(HomeActivity.this);
                View mView = getLayoutInflater().inflate(R.layout.dailog_create_group, null);
                final EditText editTextGroupName = (EditText) mView.findViewById(R.id.editText_GroupName);
                final EditText editTextGroupDescription = (EditText) mView.findViewById(R.id.editText_GroupDescription);
                FloatingActionButton button = (FloatingActionButton) mView.findViewById(R.id.fabDone_createGroup);
                button.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(!editTextGroupName.getText().toString().isEmpty() && !editTextGroupDescription.getText().toString().isEmpty()){
                            // Check for already existed GroupId
                            /*if(checkGroupNameExists(editTextGroupName.getText().toString())){
                            createGroup(editTextGroupName.getText().toString(), editTextGroupDescription.getText().toString());
                            Toast.makeText(HomeActivity.this, "Group Created...!",Toast.LENGTH_SHORT).show();
                            }else {
                                editTextGroupName.setError("Group Name is already exists.!");
                            }*/
                            createGroup(editTextGroupName.getText().toString(), editTextGroupDescription.getText().toString());
                            Toast.makeText(HomeActivity.this, "Group Created...!",Toast.LENGTH_SHORT).show();
                            dialog.dismiss();
                        }else {
                            Toast.makeText(HomeActivity.this, "Fill all the details..! ",Toast.LENGTH_SHORT).show();
                        }
                    }
                });
                mBuilder.setView(mView);
                dialog = mBuilder.create();
                dialog.show();
            }
        });
        fabCapturePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EnableRuntimePermission();
                takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
                    startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
                }
                if(hasImage(imageView)){
                    AlertDialog.Builder mBuilder = new AlertDialog.Builder(HomeActivity.this);
                    View mView = getLayoutInflater().inflate(R.layout.dailog_save_image, null);
                    final ImageView capturedImageView = (ImageView) mView.findViewById(R.id.capturedImageView);
                    final EditText editText_Caption = (EditText) mView.findViewById(R.id.editText_Caption);
                    final EditText editText_Location = (EditText) mView.findViewById(R.id.editText_Location);
                    final EditText editText_Time = (EditText) mView.findViewById(R.id.editText_Time);
                    final EditText editText_PhotoDescription = (EditText) mView.findViewById(R.id.editText_PhotoDescription);
                    capturedImageView.setImageDrawable(imageView.getDrawable());
                    final DateFormat df = new SimpleDateFormat("dd:MM:yy HH:mm:ss");
                    final Date date = new Date();
                    editText_Time.setText(df.format(date));
                    FloatingActionButton button = (FloatingActionButton) mView.findViewById(R.id.fabDone_SavePhoto);
                    button.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(!editText_Time.getText().toString().isEmpty() && !editText_PhotoDescription.getText().toString().isEmpty()
                                   && !editText_Caption.getText().toString().isEmpty() && !editText_Location.getText().toString().isEmpty()){
                                // Get the data from an ImageView as bytes
                                capturedImageView.setDrawingCacheEnabled(true);
                                capturedImageView.buildDrawingCache();
                                Bitmap bitmap = capturedImageView.getDrawingCache();
                                ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
                                byte[] data = byteArrayOutputStream.toByteArray();
                                String PhotoName = user.getUid()+"_"+df.format(date)+".jpg";
                                String photoURl = uploadPhoto(data,PhotoName,editText_Caption.getText().toString(),editText_Location.getText().toString(),editText_Time.getText().toString(),editText_PhotoDescription.getText().toString(),photoPath,user.getUid());
                                Log.d(TAG,"PhotoURL :"+photoPath);
                                //tagPhoto(PhotoName,editText_Caption.getText().toString(),editText_Location.getText().toString(),editText_Time.getText().toString(),editText_PhotoDescription.getText().toString(),photoPath,user.getUid());
                                Toast.makeText(HomeActivity.this, "Photo Saved..!",Toast.LENGTH_SHORT).show();
                                dialog.dismiss();
                            }else {
                                Toast.makeText(HomeActivity.this, "Fill all the details..! ",Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    mBuilder.setView(mView);
                    dialog = mBuilder.create();
                    dialog.show();
                }
            }
        });
        // Navigation view header
        navHeader = navigationView.getHeaderView(0);
        txtName = (TextView) navHeader.findViewById(R.id.name);
        txtEmail = (TextView) navHeader.findViewById(R.id.email);
        imgNavHeaderBg = (ImageView) navHeader.findViewById(R.id.img_header_bg);
        imgProfile = (ImageView) navHeader.findViewById(R.id.img_profile);

        // load toolbar titles from string resources
        activityTitles = getResources().getStringArray(R.array.nav_item_activity_titles);

        /*fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */
        // load nav menu header data
        loadNavHeader();

        // initializing navigation menu
        setUpNavigationView();

        if (savedInstanceState == null) {
            navItemIndex = 0;
            CURRENT_TAG = TAG_HOME;
            loadHomeFragment();
        }
    }

    private void tagPhoto(String PhotoName, String PhotoCaption, String PhotoLocation, String PhotoCreatedDateTime, String PhotoDescription, String PhotoURL, String GroupCreatedBy) {
        Photos photos = new Photos(PhotoName,PhotoCaption,PhotoLocation, PhotoCreatedDateTime,PhotoDescription,PhotoURL,GroupCreatedBy);
        String PhotoNameWithoutExtension = PhotoName.substring(0, PhotoName.indexOf('.'));
        mFirebaseDatabasePhoto.child(user.getUid()).child(PhotoNameWithoutExtension).setValue(photos);
        tagPhotoChangeListner();
    }

    private void tagPhotoChangeListner() {
        // User data change listener0
        mFirebaseDatabasePhoto.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Photos photos = dataSnapshot.getValue(Photos.class);

                // Check for null
                if (photos == null) {
                    Log.e(TAG, "Photo data is null!");
                    return;
                }

                Log.e(TAG, "Photo data is changed!" + photos.getPhotoName() + ", " + photos.getPhotoURL());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read group
                Log.e(TAG, "Failed to read photo", databaseError.toException());
            }});}

    private String uploadPhoto(byte[] data, final String photoName, final String PhotoCaption, final String PhotoLocation, final String PhotoCreatedDateTime, final String PhotoDescription, final String PhotoURL, final String GroupCreatedBy) {
        final StorageReference photoRef = mFirebaseStorage.child(photoName);
        UploadTask uploadTask = photoRef.putBytes(data);
        uploadTask.addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.d(TAG,"Photo Upload Failed..!");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                // taskSnapshot.getMetadata() contains file metadata such as size, content-type, and download URL.
                Uri downloadUrl = taskSnapshot.getDownloadUrl();
                //photoPath = downloadUrl.toString();
                //Log.d(TAG,"Photopath[0] "+ photoPath);
                tagPhoto(photoName,PhotoCaption,PhotoLocation,PhotoCreatedDateTime,PhotoDescription,downloadUrl.toString(),GroupCreatedBy);
            }
        });
        return photoPath;
    }

    private boolean hasImage(@NonNull ImageView view) {
        Drawable drawable = view.getDrawable();
        boolean hasImage = (drawable != null);

        if (hasImage && (drawable instanceof BitmapDrawable)) {
            hasImage = ((BitmapDrawable)drawable).getBitmap() != null;
        }

        return hasImage;
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            Bundle extras = data.getExtras();
            Bitmap imageBitmap = (Bitmap) extras.get("data");
            imageView.setImageBitmap(imageBitmap);
        }
    }

    public void EnableRuntimePermission(){

        /*if (ActivityCompat.shouldShowRequestPermissionRationale(HomeActivity.this,
                Manifest.permission.CAMERA))
        {

            Toast.makeText(HomeActivity.this,"CAMERA permission allows us to Access CAMERA app", Toast.LENGTH_LONG).show();

        } else {

            ActivityCompat.requestPermissions(HomeActivity.this,new String[]{
                    Manifest.permission.CAMERA}, RequestPermissionCode);

        }*/
        if (ContextCompat.checkSelfPermission(HomeActivity.this,
                Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity)
                    HomeActivity.this, Manifest.permission.CAMERA)) {


            } else {
                ActivityCompat.requestPermissions(HomeActivity.this,
                        new String[]{Manifest.permission.CAMERA},
                        MY_PERMISSIONS_REQUEST_CAMERA);
            }

        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_CAMERA: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    // permission was granted, yay! Do the
                    // contacts-related task you need to do.

                } else {

                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }
    /*@Override
    public void onRequestPermissionsResult(int RC, String per[], int[] PResult) {

        switch (RC) {

            case RequestPermissionCode:

                if (PResult.length > 0 && PResult[0] == PackageManager.PERMISSION_GRANTED) {

                    //Toast.makeText(HomeActivity.this,"Permission Granted, Now your application can access CAMERA.", Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Permission Granted, Now your application can access CAMERA.");
                } else {

                    //Toast.makeText(HomeActivity.this,"Permission Canceled, Now your application cannot access CAMERA.", Toast.LENGTH_LONG).show();
                    Log.d(TAG,"Permission Canceled, Now your application cannot access CAMERA");
                }
                break;
        }
    }*/

    private boolean checkGroupNameExists(final String GroupName) {
        final boolean[] result = new boolean[1];
        DatabaseReference rootRef = mFirebaseDatabase.child(user.getUid());
        rootRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(GroupName).exists()){
                    result[0] = false;
                }else {
                    result[0] = true;
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        return result[0];
    }

    private void createGroup(String GroupName, String GroupDescription) {
        DateFormat df = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
        Date date = new Date();
        Groups groups = new Groups(user.getUid()+"_"+GroupName,GroupName,GroupDescription,df.format(date),user.getUid());
        mFirebaseDatabase.child(user.getUid()).child(GroupName).setValue(groups);
        addGroupChangeListner();
    }

    private void addGroupChangeListner() {
        // User data change listener
        mFirebaseDatabase.child(user.getUid()).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Groups groups = dataSnapshot.getValue(Groups.class);

                // Check for null
                if (groups == null) {
                    Log.e(TAG, "Group data is null!");
                    return;
                }

                Log.e(TAG, "Group data is changed!" + groups.getGroupName() + ", " + groups.getGroupId());

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                // Failed to read group
                Log.e(TAG, "Failed to read group", databaseError.toException());
            }});}

    public void animateFAB(){

        if(isFabOpen){

            fab.startAnimation(rotate_backward);
            fabCreateGroup.startAnimation(fab_close);
            fabCapturePhoto.startAnimation(fab_close);
            fabCreateGroup.setClickable(false);
            fabCapturePhoto.setClickable(false);
            isFabOpen = false;
            Log.d("HomeActivity", "close");

        } else {

            fab.startAnimation(rotate_forward);
            fabCreateGroup.startAnimation(fab_open);
            fabCapturePhoto.startAnimation(fab_open);
            fabCreateGroup.setClickable(true);
            fabCapturePhoto.setClickable(true);
            isFabOpen = true;
            Log.d("HomeActivity","open");

        }
    }

    /***
     * Load navigation menu header information
     * like background image, profile image
     * name, website, notifications action view (dot)
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void loadNavHeader() {
        // name, email
        txtName.setText(user.getDisplayName());
        txtEmail.setText(user.getEmail());

        // loading header background image
        Glide.with(this).load("")
                .placeholder(getDrawable(R.drawable.nav_menu_header_bg))
                .crossFade()
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgNavHeaderBg);

        // Loading profile image
        Glide.with(this).load(user.getPhotoUrl())
                .crossFade()
                .thumbnail(0.5f)
                .bitmapTransform(new CircleTransform(this))
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(imgProfile);
    }

    /***
     * Returns respected fragment that user
     * selected from navigation menu
     */
    private void loadHomeFragment() {
        // selecting appropriate nav menu item
        selectNavMenu();

        // set toolbar title
        setToolbarTitle();

        // if user select the current navigation menu again, don't do anything
        // just close the navigation drawer
        if (getSupportFragmentManager().findFragmentByTag(CURRENT_TAG) != null) {
            drawer.closeDrawers();

            // show or hide the fab button
            toggleFab();
            return;
        }

        // Sometimes, when fragment has huge data, screen seems hanging
        // when switching between navigation menus
        // So using runnable, the fragment is loaded with cross fade effect
        // This effect can be seen in GMail app
        Runnable mPendingRunnable = new Runnable() {
            @Override
            public void run() {
                // update the main content by replacing fragments
                Fragment fragment = getHomeFragment();
                FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
                fragmentTransaction.setCustomAnimations(android.R.anim.fade_in,
                        android.R.anim.fade_out);
                fragmentTransaction.replace(R.id.frame, fragment, CURRENT_TAG);
                fragmentTransaction.commitAllowingStateLoss();
            }
        };

        // If mPendingRunnable is not null, then add to the message queue
        if (mPendingRunnable != null) {
            mHandler.post(mPendingRunnable);
        }

        // show or hide the fab button
        toggleFab();

        //Closing drawer on item click
        drawer.closeDrawers();

        // refresh toolbar menu
        invalidateOptionsMenu();
    }
    private Fragment getHomeFragment() {
        switch (navItemIndex) {
            case 0:
                // home
                HomeFragment homeFragment = new HomeFragment();
                return homeFragment;
            case 1:
                // photos
                PhotosFragment photosFragment = new PhotosFragment();
                return photosFragment;
            default:
                return new HomeFragment();
        }
    }

    private void setToolbarTitle() {
        getSupportActionBar().setTitle(activityTitles[navItemIndex]);
    }

    private void selectNavMenu() {
        navigationView.getMenu().getItem(navItemIndex).setChecked(true);
    }

    private void setUpNavigationView() {
        //Setting Navigation View Item Selected Listener to handle the item click of the navigation menu
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

            // This method will trigger on item Click of navigation menu
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()) {
                    //Replacing the main content with ContentFragment Which is our Inbox View;
                    case R.id.home:
                        navItemIndex = 0;
                        CURRENT_TAG = TAG_HOME;
                        break;
                    case R.id.nav_photos:
                        navItemIndex = 1;
                        CURRENT_TAG = TAG_PHOTOS;
                        break;
                    case R.id.nav_about_us:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(HomeActivity.this, AboutUsActivity.class));
                        drawer.closeDrawers();
                        return true;
                    case R.id.nav_privacy_policy:
                        // launch new intent instead of loading fragment
                        startActivity(new Intent(HomeActivity.this, PrivacyPolicyActivity.class));
                        drawer.closeDrawers();
                        return true;
                    default:
                        navItemIndex = 0;
                }

                //Checking if the item is in checked state or not, if not make it in checked state
                if (menuItem.isChecked()) {
                    menuItem.setChecked(false);
                } else {
                    menuItem.setChecked(true);
                }
                menuItem.setChecked(true);

                loadHomeFragment();

                return true;
            }
        });


        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.openDrawer, R.string.closeDrawer) {

            @Override
            public void onDrawerClosed(View drawerView) {
                // Code here will be triggered once the drawer closes as we dont want anything to happen so we leave this blank
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                // Code here will be triggered once the drawer open as we dont want anything to happen so we leave this blank
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawer.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessary or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawers();
            return;
        }

        // This code loads home fragment when back key is pressed
        // when user is in other fragment than home
        if (shouldLoadHomeFragOnBackPress) {
            // checking if user is on other navigation menu
            // rather than home
            if (navItemIndex != 0) {
                navItemIndex = 0;
                CURRENT_TAG = TAG_HOME;
                loadHomeFragment();
                return;
            }
        }

        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.

        // show menu only when home fragment is selected
        if (navItemIndex == 0) {
            getMenuInflater().inflate(R.menu.main, menu);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_logout) {
            mAuth.getInstance().signOut();
            Intent intent = new Intent(this,MainActivity.class);
            Toast.makeText(getApplicationContext(), "Logout user!", Toast.LENGTH_LONG).show();
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
    // show or hide the fab
    private void toggleFab() {
        if (navItemIndex == 0)
            fab.show();
        else
            fab.hide();
    }

}
