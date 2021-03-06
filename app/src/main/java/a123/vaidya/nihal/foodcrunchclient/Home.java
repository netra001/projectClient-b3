package a123.vaidya.nihal.foodcrunchclient;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.daimajia.slider.library.Animations.DescriptionAnimation;
import com.daimajia.slider.library.SliderLayout;
import com.daimajia.slider.library.SliderTypes.BaseSliderView;
import com.daimajia.slider.library.SliderTypes.TextSliderView;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.messaging.FirebaseMessaging;
import com.rengwuxian.materialedittext.MaterialEditText;
import com.rey.material.widget.Slider;
import com.squareup.picasso.Picasso;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import a123.vaidya.nihal.foodcrunchclient.Common.Common;
import a123.vaidya.nihal.foodcrunchclient.Database.Database;
import a123.vaidya.nihal.foodcrunchclient.Interface.ItemClickListener;
import a123.vaidya.nihal.foodcrunchclient.Model.Banner;
import a123.vaidya.nihal.foodcrunchclient.Model.Category;
import a123.vaidya.nihal.foodcrunchclient.Model.MyResponse;
//import a123.vaidya.nihal.foodcrunchclient.Model.Notification;
import a123.vaidya.nihal.foodcrunchclient.Model.Request;
//import a123.vaidya.nihal.foodcrunchclient.Model.Sender;
import a123.vaidya.nihal.foodcrunchclient.Model.Token;
import a123.vaidya.nihal.foodcrunchclient.Remote.APIService;
import a123.vaidya.nihal.foodcrunchclient.ViewHolder.MenuViewHolder;
import dmax.dialog.SpotsDialog;
import io.paperdb.Paper;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;


public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private FirebaseDatabase database;
    private DatabaseReference category;
    private TextView txtFullName,txtemail,txtPhone;
    MaterialEditText edtHomeAddress,edtPassword,edtEmail,edtName;
    //caligraphy font install
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    private RecyclerView recycler_menu;
    RecyclerView.LayoutManager layoutManager;
    private FirebaseRecyclerAdapter<Category,MenuViewHolder> adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private CounterFab fab;
    APIService mAPIService;
    //slider
    HashMap<String,String>img_list;
    SliderLayout mslider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //caligraphy font init
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_home);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("MENU");
        setSupportActionBar(toolbar);
        mAPIService = Common.getFCMClient();
        //firebase
        swipeRefreshLayout = findViewById(R.id.swipelayout1);
        swipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
        android.R.color.holo_green_dark,
        android.R.color.holo_orange_dark,
        android.R.color.holo_red_dark,
        android.R.color.holo_blue_dark);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    loadMenu();
                }
                else
                {
                    Toast.makeText(getBaseContext(),"Please check your internet connection",Toast.LENGTH_LONG).show();
                }
            }
        });
        swipeRefreshLayout.post(new Runnable() {
            @Override
            public void run() {
                if (Common.isConnectedToInternet(getBaseContext())) {

                    loadMenu();
                }
                else
                {
                    Toast.makeText(getBaseContext(),"Please check your internet connection",Toast.LENGTH_LONG).show();
                }
            }
        });
        Twitter.initialize(this);
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("6ep60jj09lvUcHncYM3yCoIMr", "WXvH93jw1urHD9IzIk6FDRmKW0X5LGZgmMCDo67XFk2uDf2LGJ"))
                .debug(true)
                .build();
        Twitter.initialize(config);
        Paper.init(this);
        database=FirebaseDatabase.getInstance();
        category=database.getReference("Category");
        //create animation immedieately after getting database
        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
                .setQuery(category, Category.class)
                .build();

        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
            @Override
            public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
                View itemView = LayoutInflater.from(parent.getContext())
                        .inflate(R.layout.menu_item,parent,false);
                return  new MenuViewHolder(itemView);
            }
            @Override
            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
                viewHolder.txtMenuName.setText(model.getName());
                Picasso.with(getBaseContext()).load(model.getImage())
                        .into(viewHolder.imageView);
                viewHolder.setItemClickListener(new ItemClickListener() {
                    @Override
                    public void onClick(View v, int position, boolean isLongClick) {
                        //category is key
                        final SpotsDialog dialog = new SpotsDialog(Home.this);
                        dialog.show();
                        Intent foodList = new Intent (Home.this,FoodList.class);
                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
                        startActivity(foodList);
                        dialog.dismiss();
                        //Toast.makeText(Home.this,""+clickItem.getName(),Toast.LENGTH_SHORT).show();
                    }
                });
            }
        };

        fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final SpotsDialog dialog = new SpotsDialog(Home.this);
                dialog.show();
                Intent cartIntent = new Intent(Home.this,Cart.class);
                startActivity(cartIntent);
                dialog.dismiss();

            }
        });
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        View headerView = navigationView.getHeaderView(0);
        txtFullName = headerView.findViewById(R.id.txtFullName);try{
        txtemail = headerView.findViewById(R.id.txtEmail);}catch (Exception e){}
        txtPhone = headerView.findViewById(R.id.txtPhone);
        txtFullName.setText(Common.currentUser.getName());//slider name
        txtemail.setText(Common.currentUser.getEmail());
        txtPhone.setText(Common.currentUser.getPhone());
        //load menu
        recycler_menu = findViewById(R.id.recycler_menu);
        recycler_menu.setHasFixedSize(true);
        recycler_menu.setLayoutManager(new GridLayoutManager(this,1));
        LayoutAnimationController controller = AnimationUtils.loadLayoutAnimation(recycler_menu.getContext(),
                R.anim.layout_fall_down);
        recycler_menu.setLayoutAnimation(controller);
        if (Common.isConnectedToInternet(this)) {

            loadMenu();
        }
        else
        {
            Toast.makeText(this,"Please check your internet connection",Toast.LENGTH_LONG).show();
            return;
        }
        //registeration of notification service
        updateToken(FirebaseInstanceId.getInstance().getToken());

        //set slider call last after inti database or carsh
        setupSlider();

    }

    private void setupSlider() {
        mslider =(SliderLayout)findViewById(R.id.slider);
        img_list = new HashMap<>();

        final DatabaseReference banners = database.getReference("Banner");
        banners.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Banner banner = postSnapshot.getValue(Banner.class);

                    img_list.put(banner.getName()+"@@@"+banner.getId(),banner.getImage());
                }
                for(String key:img_list.keySet())
                {
                    String[] keysplit = key.split("@@@");
                    String nameofFoood = keysplit[0];
                    String idofthefood = keysplit[1];
                    //name of image is name_foodid

                    //create slider
                    final TextSliderView textSliderView =new TextSliderView(getBaseContext());
                    textSliderView.description(nameofFoood)
                            .image(img_list.get(key))
                            .setScaleType(BaseSliderView.ScaleType.Fit)
                            .setOnSliderClickListener(new BaseSliderView.OnSliderClickListener() {
                                @Override
                                public void onSliderClick(BaseSliderView slider) {
                               Intent intent = new Intent(Home.this,FoodDetail.class);
                               //pass food id to inetnt
                                  intent.putExtras(textSliderView.getBundle());
                                  startActivity(intent);
                                }
                            });
                    //add extra bundle to intent
                    textSliderView.bundle(new Bundle());
                    textSliderView.getBundle().putString("FoodId",idofthefood);
                    mslider.addSlider(textSliderView);

                    //remove after finish
                    banners.removeEventListener(this);

                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mslider.setPresetTransformer(SliderLayout.Transformer.Background2Foreground);
        mslider.setPresetIndicator(SliderLayout.PresetIndicators.Center_Bottom);
        mslider.setCustomAnimation(new DescriptionAnimation());
        mslider.setDuration(4000);


    }


    private void updateToken(String token) {
        FirebaseDatabase db = FirebaseDatabase.getInstance();
        DatabaseReference tokens = db.getReference("Tokens");
        Token data = new Token(token,false);//false as this reads frm client
        try {
            tokens.child(Common.currentUser.getPhone()).setValue(data);
            Toast.makeText(Home.this,"Welcome ",Toast.LENGTH_LONG).show();

        }
        catch(Exception e){
            Toast.makeText(Home.this,"your phone no is missing",Toast.LENGTH_LONG).show();
        }
    }

    private void loadMenu() {
        //new firebase code
//        FirebaseRecyclerOptions<Category> options = new FirebaseRecyclerOptions.Builder<Category>()
//                .setQuery(category, Category.class)
//                .build();
//
//        adapter = new FirebaseRecyclerAdapter<Category, MenuViewHolder>(options) {
//            @Override
//            public MenuViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
//                View itemView = LayoutInflater.from(parent.getContext())
//                        .inflate(R.layout.menu_item,parent,false);
//                return  new MenuViewHolder(itemView);
//            }
//            @Override
//            protected void onBindViewHolder(@NonNull MenuViewHolder viewHolder, int position, @NonNull Category model) {
//                viewHolder.txtMenuName.setText(model.getName());
//                Picasso.with(getBaseContext()).load(model.getImage())
//                        .into(viewHolder.imageView);
//                final Category clickItem = model;
//                viewHolder.setItemClickListener(new ItemClickListener() {
//                    @Override
//                    public void onClick(View v, int position, boolean isLongClick) {
//                        //category is key
//                        final SpotsDialog dialog = new SpotsDialog(Home.this);
//                        dialog.show();
//                        Intent foodList = new Intent (Home.this,FoodList.class);
//                        foodList.putExtra("CategoryId",adapter.getRef(position).getKey());
//                        startActivity(foodList);
//                        dialog.dismiss();
//                        //Toast.makeText(Home.this,""+clickItem.getName(),Toast.LENGTH_SHORT).show();
//                    }
//                });
//            }
//        };
        adapter.startListening();
        recycler_menu.setAdapter(adapter);
        swipeRefreshLayout.setRefreshing(false);

        //aniamtion begins
        recycler_menu.getAdapter().notifyDataSetChanged();
        recycler_menu.scheduleLayoutAnimation();
        }

    @Override
    protected void onStart() {
        super.onStart();
        loadMenu();
        adapter.startListening();
        adapter.notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);
    }
//
    @Override
    protected void onResume() {
        super.onResume();
        fab.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        loadMenu();
        if (adapter!= null){
        adapter.startListening();}
        Objects.requireNonNull(adapter).notifyDataSetChanged();
        recycler_menu.setAdapter(adapter);

    }

    @Override
    protected void onStop() {
        super.onStop();
        mslider.stopAutoCycle();
        //adapter.stopListening();
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.home, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (item.getItemId() == R.id.refresh)
        {
            loadMenu();
        }else if (item.getItemId() == R.id.menu_search)
        {
            startActivity(new Intent(Home.this,SearchActivity.class));
        }else if (item.getItemId() == R.id.menu_info)
        {
            Intent tutorial = new Intent(Home.this, Tutorial.class);
            startActivity(tutorial);

        }
        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        switch (id) {
            case R.id.nav_menu:
                Toast.makeText(Home.this, "You are already in main menu", Toast.LENGTH_LONG).show();
                break;
            case R.id.nav_cart: {
                final SpotsDialog dialog = new SpotsDialog(Home.this);
                Intent cartIntent = new Intent(Home.this, Cart.class);
                dialog.dismiss();
                startActivity(cartIntent);
                break;
            }
            case R.id.nav_orders: {
                final SpotsDialog dialog = new SpotsDialog(Home.this);
                Intent orderIntent = new Intent(Home.this, OrderStatus.class);
                startActivity(orderIntent);
                dialog.dismiss();
                break;
            }
            case R.id.nav_send_login_details: {
                sendemailUSER();
                break;
            }
            case R.id.nav_list: {
                final SpotsDialog dialog = new SpotsDialog(Home.this);
                Intent orderIntent = new Intent(Home.this, TodoList.class);
                startActivity(orderIntent);
                dialog.dismiss();
                break;}
            case R.id.nav_chat: {
                final SpotsDialog dialog = new SpotsDialog(Home.this);
                Intent about = new Intent(Home.this, ChatActivity.class);
                startActivity(about);
                dialog.dismiss();
                break;
            }
            case R.id.nav_about: {
                final SpotsDialog dialog = new SpotsDialog(Home.this);
                Intent about = new Intent(Home.this, About.class);
                startActivity(about);
                dialog.dismiss();
                break;
            }
            case R.id.nav_logout: {
                //delete remmbered user details
                final SpotsDialog dialog = new SpotsDialog(Home.this);
                Toast.makeText(Home.this, "Logging out", Toast.LENGTH_LONG).show();
                Paper.book().destroy();

                Intent signIn = new Intent(Home.this, Signin.class);
                signIn.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(signIn);
                dialog.dismiss();
                break;
            }
            case R.id.nav_favorites: {
                final SpotsDialog dialog = new SpotsDialog(Home.this);
                Intent orderIntent = new Intent(Home.this, ShowFavorites.class);
                startActivity(orderIntent);
                dialog.dismiss();
                break;
            }
//            case R.id.nav_settings: {
//                showSettingsDailog();
//                break;
//            }
            case R.id.nav_homeaddress:{
                showHomeAddressDialog();
                break;}
            case R.id.nav_emailaddress:{
                showEmailAddressDialog();
                break;}
            case R.id.nav_password:{
                showChangePasswordDialog();
                break;}
            case R.id.nav_name:{
                showChangeNameDialog();
                break;}

        }
        DrawerLayout drawer = findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    private void showSettingsDailog() {

        AlertDialog.Builder alertDailog = new AlertDialog.Builder(Home.this);
        alertDailog.setTitle("SETTINGS");
        alertDailog.setCancelable(false);
        alertDailog.setIcon(R.drawable.ic_power_settings_new_black_24dp);
        alertDailog.setMessage("TAKE THE APP IN YOUR CONTROL");
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_setting = inflater.inflate(R.layout.settings_layout,null);
//        final SpotsDialog dialog1 = new SpotsDialog(Home.this);
//        dialog1.show();
//        final MaterialEditText edtHomeAddress = layout_home.findViewById(R.id.edtHomeAddress);
        final CheckBox chk_sub = (CheckBox)layout_setting.findViewById(R.id.chk_subscribe);
        //remember state of checkbox
        Paper.init(this);
        String isSubscribed = Paper.book().read("sub_new");
        if(isSubscribed == null || TextUtils.isEmpty(isSubscribed) || isSubscribed.equals("false"))  //can cause crash if no text utils
            chk_sub.setChecked(false);
        else
            chk_sub.setChecked(true);


        alertDailog.setView(layout_setting);
        alertDailog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

                if(chk_sub.isChecked())
                {
                    FirebaseMessaging.getInstance().subscribeToTopic(Common.topicName);
                    Paper.book().write("sub_new","true");//written converted to string
                }else{
                    FirebaseMessaging.getInstance().unsubscribeFromTopic(Common.topicName);
                Paper.book().write("sub_new","false");//written converted to string
                     }

            }
        });
        alertDailog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDailog.show();
    }

    private void showChangeNameDialog() {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
        alertDialog.setTitle("CHANGE NAME");
        alertDialog.setCancelable(false);
        alertDialog.setIcon(R.drawable.ic_child_care_black_24dp);
        alertDialog.setMessage("LOG OUT FOR EFFECT");
//        final SpotsDialog dialog1 = new SpotsDialog(Home.this);
//        dialog1.show();
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_name = inflater.inflate(R.layout.user_name_layout,null);
        final MaterialEditText edtName = layout_name.findViewById(R.id.edtuser_name);
        alertDialog.setView(layout_name);
        alertDialog.setPositiveButton("UPDATE!!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Map<String, Object> NameUpdate = new HashMap<>();
                NameUpdate.put("name", edtName.getText().toString());
                DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                user.child(Common.currentUser.getPhone())
                        .updateChildren(NameUpdate)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
//                                dialog1.dismiss();
                                if (task.isSuccessful())
                                    Toast.makeText(Home.this, "Update name successful", Toast.LENGTH_LONG).show();

                            }
                        });
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void sendemailUSER() {
        Toast.makeText(Home.this,"Please select the way you want tor recieve detaisl",Toast.LENGTH_LONG).show();
        String[] TO = {Common.currentUser.getEmail().toString()};
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setData(Uri.parse("mailto:"));
        emailIntent.setType("text/plain");
        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
        //emailIntent.putExtra(Intent.EXTRA_CC, CC);
        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "You have created a staff account with FoodCrunch the anytime shopping app");
        emailIntent.putExtra(Intent.EXTRA_TEXT, "Here are your account details \n"+
                "The new account is created for the staff member \t" +
                (Common.currentUser.getName().toString())+
                "\n with email \t" +
                (Common.currentUser.getEmail().toString())+
                "\nand has been linked to your phone number \n" +
                "\n \n Your password is  \t" +
                (Common.currentUser.getPassword().toString())+
                "\n \n and your secure code is \t" +
                (Common.currentUser.getSecureCode().toString())+
                "\n\nPlease write down your secure code. \nIt will be used to recover your password");
        try {
            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
        } catch (android.content.ActivityNotFoundException ex) {
             Toast.makeText(Home.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEmailAddressDialog() {
        AlertDialog.Builder alertDailog = new AlertDialog.Builder(Home.this);
        alertDailog.setTitle("CHANGE EMAIL ADDRESS");
        alertDailog.setCancelable(false);
        alertDailog.setIcon(R.drawable.ic_email_black_24dp);
        alertDailog.setMessage("LOG OUT FOR EFFECT");
        LayoutInflater inflater = LayoutInflater.from(this);
//        final SpotsDialog dialog1 = new SpotsDialog(Home.this);
//        dialog1.show();
        View layout_email = inflater.inflate(R.layout.email_address_layout,null);
        final MaterialEditText edtEmail = layout_email.findViewById(R.id.edtEmailAddress);
        alertDailog.setView(layout_email);
        alertDailog.setPositiveButton("UPDATE!!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                Map<String, Object> EmailUpdate = new HashMap<>();
                EmailUpdate.put("email", edtEmail.getText().toString());
                DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                user.child(Common.currentUser.getPhone())
                        .updateChildren(EmailUpdate)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
//                                dialog1.dismiss();
                                if (task.isSuccessful())
                                    Toast.makeText(Home.this, "Update email successful", Toast.LENGTH_LONG).show();

                            }
                        });
            }
        });
        alertDailog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();

            }
        });
        alertDailog.show();
    }

    private void showChangePasswordDialog() {
    AlertDialog.Builder alertDialog = new AlertDialog.Builder(Home.this);
    alertDialog.setTitle("CHANGE PASSWORD");
    alertDialog.setCancelable(false);
    alertDialog.setIcon(R.drawable.ic_security_black_24dp);
    alertDialog.setMessage("LOG OUT FOR EFFECT");

        LayoutInflater inflater = LayoutInflater.from(this);
        View email_address_layout = inflater.inflate(R.layout.email_address_layout,null);
        View layout_pwd = inflater.inflate(R.layout.change_password_layout,null);
        final MaterialEditText edtPassword = layout_pwd.findViewById(R.id.edtPassword);
        final MaterialEditText edtNewPassword = layout_pwd.findViewById(R.id.edtNewPassword);
        final MaterialEditText edtRepeatPassword = layout_pwd.findViewById(R.id.edtRepeatNewPassword);

        alertDialog.setView(layout_pwd);
        alertDialog.setPositiveButton("UPDATE!!", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final SpotsDialog dialog1 = new SpotsDialog(Home.this);
                dialog1.show();
                    //check old password
                if(edtPassword.getText().toString().equals(Common.currentUser.getPassword()))
                {
                    //check new password
                    if((edtNewPassword.getText().toString().equals(edtRepeatPassword.getText().toString()))
                            &&(! edtNewPassword.getText().toString().isEmpty())
                            )
                    {
                        if(! edtNewPassword.getText().toString().equals(edtPassword.getText().toString()))
                        {
                            Map <String, Object> passwordUpdate = new HashMap<>();
                            passwordUpdate.put("Password", edtNewPassword.getText().toString());
                            DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                            user.child(Common.currentUser.getPhone())
                                    .updateChildren(passwordUpdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {
                                            dialog1.dismiss();
                                            Toast.makeText(Home.this, "Your password was updated", Toast.LENGTH_LONG).show();

                                        }
                                    })
                                    .addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            dialog1.dismiss();
                                            Toast.makeText(Home.this, "Problem updating password buddy", Toast.LENGTH_LONG).show();

                                        }
                                    })
                            ;
                        }
                        else
                        {
                            dialog1.dismiss();
                            Toast.makeText(Home.this,"New password is same as the old one",Toast.LENGTH_LONG).show();
                        }
                    }else
                    {
                        dialog1.dismiss();
                        Toast.makeText(Home.this,"Password doesnt match!\n Please try again",Toast.LENGTH_LONG).show();

                    }
                }else
                {
                    dialog1.dismiss();
                    Toast.makeText(Home.this,"Wrong Password!!\n Please try Again",Toast.LENGTH_LONG).show();
                }
            }
        });
        alertDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
           dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    private void showHomeAddressDialog() {
        AlertDialog.Builder alertDailog = new AlertDialog.Builder(Home.this);
        alertDailog.setTitle("CHANGE HOME ADDRESS");
        alertDailog.setCancelable(false);
        alertDailog.setIcon(R.drawable.ic_home_black_24dp);
        alertDailog.setMessage("LOG OUT FOR EFFECT");
        LayoutInflater inflater = LayoutInflater.from(this);
        View layout_home = inflater.inflate(R.layout.home_address_layout,null);
//        final SpotsDialog dialog1 = new SpotsDialog(Home.this);
//        dialog1.show();
        final MaterialEditText edtHomeAddress = layout_home.findViewById(R.id.edtHomeAddress);
        alertDailog.setView(layout_home);
        alertDailog.setPositiveButton("UPDATE!!", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        //set new home address
                        //   if((edtHomeAddress.getText().toString() != null) &&(!TextUtils.isEmpty((CharSequence) edtHomeAddress)) ){
                        Map<String, Object> HomeUpdate = new HashMap<>();
                        HomeUpdate.put("homeAddress", edtHomeAddress.getText().toString());
                        DatabaseReference user = FirebaseDatabase.getInstance().getReference("User");
                        user.child(Common.currentUser.getPhone())
                                .updateChildren(HomeUpdate)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
//                                        dialog1.dismiss();
                                        if (task.isSuccessful())
                                            Toast.makeText(Home.this, "Update home address successful", Toast.LENGTH_LONG).show();

                                    }
                                });
                    }
                });
        alertDailog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDailog.show();
    }
}
