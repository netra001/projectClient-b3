
package a123.vaidya.nihal.foodcrunchclient;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.CollapsingToolbarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.andremion.counterfab.CounterFab;
import com.cepheuen.elegantnumberbutton.view.ElegantNumberButton;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import a123.vaidya.nihal.foodcrunchclient.Common.Common;
import a123.vaidya.nihal.foodcrunchclient.Database.Database;
import a123.vaidya.nihal.foodcrunchclient.Model.Food;
import a123.vaidya.nihal.foodcrunchclient.Model.Order;
import a123.vaidya.nihal.foodcrunchclient.Model.Rating;
import a123.vaidya.nihal.foodcrunchclient.Model.User;
import info.hoang8f.widget.FButton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

import com.luseen.autolinklibrary.AutoLinkMode;
import com.luseen.autolinklibrary.AutoLinkOnClickListener;
import com.luseen.autolinklibrary.AutoLinkTextView;
import com.muddzdev.styleabletoastlibrary.ToastDurationWatcher;
import com.squareup.picasso.Picasso;
import com.stepstone.apprating.AppRatingDialog;
import com.stepstone.apprating.listener.RatingDialogListener;
import com.twitter.sdk.android.core.DefaultLogger;
import com.twitter.sdk.android.core.Twitter;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterConfig;

import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class FoodDetail extends AppCompatActivity implements RatingDialogListener{

    private TextView food_name;
    private TextView food_price;
    private TextView food_description;
    private TextView food_video;
    private AutoLinkTextView food_recepie;
    private ImageView food_image;
    private CollapsingToolbarLayout collapsingToolbarLayout;
    private FloatingActionButton btnRating;
    private CounterFab btnCart;
    private ElegantNumberButton numberButton;
    private RatingBar ratingBar;

    //caligraphy font install
    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }
    private String foodId="";
    String categoryId ="";
    private FirebaseDatabase database;
    private DatabaseReference foods;
    FButton btnSohowComment;
    private DatabaseReference ratingTbl;

    private Food currentFood;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //caligraphy font init
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/restaurant_font.otf")
                .setFontAttrId(R.attr.fontPath)
                .build());
        setContentView(R.layout.activity_food_detail);

        btnSohowComment = (FButton)findViewById(R.id.bthshowcomments);
        btnSohowComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(FoodDetail.this,ShowComment.class);
                intent.putExtra(Common.INTENT_FOOD_ID,foodId);
                        startActivity(intent);
            }
        });

        //Firebase code
        Twitter.initialize(this);
        TwitterConfig config = new TwitterConfig.Builder(this)
                .logger(new DefaultLogger(Log.DEBUG))
                .twitterAuthConfig(new TwitterAuthConfig("6ep60jj09lvUcHncYM3yCoIMr", "WXvH93jw1urHD9IzIk6FDRmKW0X5LGZgmMCDo67XFk2uDf2LGJ"))
                .debug(true)
                .build();
        Twitter.initialize(config);
        database = FirebaseDatabase.getInstance();
        foods = database.getReference("Foods");
        ratingTbl = database.getReference("Rating");

        //actual view
        numberButton = findViewById(R.id.number_button);
        btnCart = findViewById(R.id.btnCart);
        btnRating = findViewById(R.id.btnRating);
        ratingBar = findViewById(R.id.ratingbar);

        btnRating.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRatingDialog();

            }
        });

        btnCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

//               Common.currentUser.getPhone(),
//                                        adapter.getRef(position).getKey(),//this gets the random id of food id this took me 2 days to debug lol
//                                        model.getName(),
//                                        "1",
//                                        model.getPrice(),
//                                        model.getDiscount(),
//                                        model.getImage(),
//                                        model.getEmail()

                new Database(getBaseContext()).addToCart(new Order
                        (Common.currentUser.getPhone(),
                                foodId,
                        //below one cgets last one item updated if multiple items added on food details fab add tp cart
//                                currentFood.getFoodId(),
                                currentFood.getName(),
                                numberButton.getNumber(),
                        currentFood.getPrice(),
                                currentFood.getEmail(),
                                currentFood.getDiscount(),
                                currentFood.getImage()

                ));
                if((currentFood.getQuantity()+50.0) > Integer.valueOf(numberButton.getNumber()) )
                {
                    double balance = currentFood.getQuantity() - Integer.valueOf(numberButton.getNumber());
                    //set to database
                    Map<String ,Object> update_balance = new HashMap<>();
                    update_balance.put("quantity",balance);

                    //get instance and put
                    FirebaseDatabase.getInstance().getReference("Foods")
                            .child(foodId)
                            //.equalTo() //if doesnt work try get curent category id
                            .updateChildren(update_balance)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if(task.isSuccessful())
                                    {
                                        //get instance and update
                                        FirebaseDatabase.getInstance().getReference("Foods")
                                                .child(foodId)
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(DataSnapshot dataSnapshot) {
//                                                        food_price.setText(currentFood.getPrice());
                Toast.makeText(FoodDetail.this, "INVENTORY UPDATED", Toast.LENGTH_LONG).show();
                                                        currentFood = dataSnapshot.getValue(Food.class);

                                                    }

                                                    @Override
                                                    public void onCancelled(DatabaseError databaseError) {

                                                    }
                                                });
                                    }
                                }
                            });
                    Toast.makeText(FoodDetail.this,"Item was added to cart",Toast.LENGTH_LONG).show();
                }else {
                    Toast.makeText(FoodDetail.this, "INVENTORY EMPTY", Toast.LENGTH_LONG).show();
                }


            }
        });
//        currentFood.getQuantity()-1.0;
        btnCart.setCount(new Database(this).getCountCart(Common.currentUser.getPhone()));
        food_description = findViewById(R.id.food_description);
        food_price = findViewById(R.id.food_price);
        food_video = findViewById(R.id.food_video);
        food_recepie = (AutoLinkTextView) findViewById(R.id.food_recepie);
        food_recepie.setHashtagModeColor(ContextCompat.getColor(this, R.color.com_facebook_blue));
        food_recepie.setPhoneModeColor(ContextCompat.getColor(this, R.color.fbutton_color_green_sea));
       // food_recepie.setCustomModeColor(ContextCompat.getColor(this, R.color.yourColor));
        food_recepie.setUrlModeColor(ContextCompat.getColor(this, R.color.fbutton_color_sun_flower));
        food_recepie.setMentionModeColor(ContextCompat.getColor(this, R.color.fbutton_color_wisteria));
        food_recepie.setEmailModeColor(ContextCompat.getColor(this, R.color.fbutton_color_pomegranate));
//        food_recepie.enableUnderLine();
        food_recepie.setSelectedStateColor(ContextCompat.getColor(this, R.color.fbutton_color_concrete));
        food_recepie.addAutoLinkMode(
                AutoLinkMode.MODE_HASHTAG,
                AutoLinkMode.MODE_EMAIL,
                AutoLinkMode.MODE_PHONE,
                AutoLinkMode.MODE_URL,
                AutoLinkMode.MODE_MENTION);
        food_recepie.setAutoLinkOnClickListener(new AutoLinkOnClickListener() {
            @Override
            public void onAutoLinkTextClick(AutoLinkMode autoLinkMode, String matchedText) {
                    if(autoLinkMode == AutoLinkMode.MODE_EMAIL)
                    {
                        String[] TO = {matchedText};
                        String[] CC = {matchedText};
                        Intent emailIntent = new Intent(Intent.ACTION_SEND);

                        emailIntent.setData(Uri.parse("mailto:"));
                        emailIntent.setType("text/plain");
                        emailIntent.putExtra(Intent.EXTRA_EMAIL, TO);
                        emailIntent.putExtra(Intent.EXTRA_CC, CC);
                        emailIntent.putExtra(Intent.EXTRA_SUBJECT, "Feedback reguarding your app ");
//                        emailIntent.putExtra(Intent.EXTRA_TEXT, "Here are your order details \n"+
//                                "The order for user  \t" +
//                                (Common.currentUser.getName())+
//                                "\nwith email  \t" +
//                                (Common.currentUser.getEmail())+
//                                "\nand phone no \t" +
//                                (Common.currentUser.getPhone())+
//                                "\n \n HAS BEEN PLACED!!  \t" +
////                                    "\n  It will be delivered to address  \t" +
////                                    (Common.currentUser.get)+
//                                "\n\nTHANK YOU FOR SHOPPING WITH US!!")
                                    ;
                        try {
                            startActivity(Intent.createChooser(emailIntent, "Send mail..."));
                        } catch (android.content.ActivityNotFoundException ex) {
                            Toast.makeText(getApplicationContext(), "There are no email clients installed.", Toast.LENGTH_SHORT);
                        }


                    }else if(autoLinkMode == AutoLinkMode.MODE_HASHTAG)
                    {

                        Toast.makeText(FoodDetail.this,matchedText, Toast.LENGTH_LONG).show();

                    }else if(autoLinkMode == AutoLinkMode.MODE_MENTION)
                    {

                        Toast.makeText(FoodDetail.this,matchedText, Toast.LENGTH_LONG).show();

                    } else if(autoLinkMode == AutoLinkMode.MODE_PHONE)
                    {
                        Toast.makeText(FoodDetail.this,matchedText, Toast.LENGTH_LONG).show();

//                        Intent callIntent = new Intent(Intent.ACTION_CALL);
//                        callIntent.setData(Uri.parse(matchedText));
//
//                        if (ActivityCompat.checkSelfPermission(getApplicationContext(),
//                                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
//                            Toast.makeText(FoodDetail.this,"PLEASE GIVE PERMISSION TO CALL FROM SETTINGS", Toast.LENGTH_LONG).show();
//
//                            return;
//                        }
//                        startActivity(callIntent);


                    } else if(autoLinkMode == AutoLinkMode.MODE_URL)
                    {

                        Toast.makeText(FoodDetail.this,matchedText, Toast.LENGTH_LONG).show();
//                        Intent intent= new Intent(Intent.ACTION_VIEW,Uri.parse(matchedText));
//                        startActivity(intent);


                    }

            }
        });


        food_name = findViewById(R.id.food_name);
        food_image= findViewById(R.id.img_food);
        collapsingToolbarLayout= findViewById(R.id.collapsing);
        collapsingToolbarLayout.setExpandedTitleTextAppearance(R.style.ExpandededAppBar);
        collapsingToolbarLayout.setCollapsedTitleTextAppearance(R.style.CollapsedAppBar);


        if (getIntent()!= null)
            foodId=getIntent().getStringExtra("FoodId");
        if(!foodId.isEmpty())
        {
            if (Common.isConnectedToInternet(getBaseContext())){
                getDetailFood(foodId);
                getRatingFood(foodId);}
            else
                Toast.makeText(FoodDetail.this,"Please check your internet connection",Toast.LENGTH_LONG).show();
        }
    }

    private void getRatingFood(String foodId) {
        Query foodRating = ratingTbl.orderByChild("foodId").equalTo(foodId);
        foodRating .addValueEventListener(new ValueEventListener() {
            int count = 0,sum=0;
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for(DataSnapshot postSnapshot:dataSnapshot.getChildren())
                {
                    Rating item = postSnapshot.getValue(Rating.class);
                    sum+=Integer.parseInt(Objects.requireNonNull(item).getRateValue());
                    count++;
                }
                if(count != 0) {
                    float average = sum / count;
                    ratingBar.setRating(average);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void showRatingDialog() {
        new AppRatingDialog.Builder()
                .setPositiveButtonText("Submit")
                .setNegativeButtonText("Cancel")
                .setNoteDescriptions(Arrays.asList("Very bad","Needs Improvement","OK","Very Good","Above Expectations"))
                .setDefaultRating(1)
                .setTitle("Please Rate Our Food!")
                .setDescription("Select a star and give review")
                .setTitleTextColor(R.color.colorPrimary)
                .setDescriptionTextColor(R.color.colorPrimary)
                .setHint("Provide Feedback Here")
                .setHintTextColor(R.color.colorAccent)
                .setCommentTextColor(R.color.white)
                .setCommentBackgroundColor(R.color.colorPrimaryDark)
                .setWindowAnimation(R.style.RatingDialogFadeAnim)
                .create(FoodDetail.this)
                .show();

    }



    private void getDetailFood(String foodId) {
        foods.child(foodId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                currentFood = dataSnapshot.getValue(Food.class);
                Picasso.with(getBaseContext()).load(currentFood.getImage()).into(food_image);
                collapsingToolbarLayout.setTitle(currentFood.getName());
                food_price.setText(currentFood.getPrice());
                food_name.setText(currentFood.getName ());
                food_description.setText(currentFood.getDescription());
                food_video.setText(currentFood.getVideo ());
                food_recepie.setAutoLinkText(currentFood.getRecepixes()+"\n\n#foodie IF THE INFO NEEDS TO BE" +
                        " UPDATED YOU CAN CONTACT ME ON 7208680470 OR EMAIL ME ON nhlvcam@gmail.com ALL INFO IS FROM" +
                        " @Wikipedia ARTICLES IN  http://www.wikipedia.org ");


            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onPositiveButtonClicked(int value, @NotNull String comments) {
        //upload rating to firebase
        final Rating rating = new Rating(Common.currentUser.getName(),
                foodId,String.valueOf(value),comments);
        ratingTbl.push()
                .setValue(rating)
                .addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        Toast.makeText(FoodDetail.this,"Thank you for your feedback!!",Toast.LENGTH_LONG).show();
                    }
                });


//old code user can rate only once
//            ratingTbl.child(Common.currentUser.getPhone()).addValueEventListener(new ValueEventListener() {
//                @Override
//                public void onDataChange(DataSnapshot dataSnapshot) {
//                    if(dataSnapshot.child(Common.currentUser.getPhone()).exists())
//                    {
//                        //remove old and update
//                        ratingTbl.child(Common.currentUser.getPhone()).removeValue();
//                        ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
//
//                    }else
//                    {
//                        ratingTbl.child(Common.currentUser.getPhone()).setValue(rating);
//                    }
//
//
//                }
//
//
//                @Override
//                public void onCancelled(DatabaseError databaseError) {
//
//                }
//
//
//            });
    }

    @Override
    public void onNegativeButtonClicked() {

    }

    @Override
    public void onNeutralButtonClicked() {

    }
}
