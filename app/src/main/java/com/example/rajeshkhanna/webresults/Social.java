package com.example.rajeshkhanna.webresults;

import android.Manifest;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyPermanentlyInvalidatedException;
import android.security.keystore.KeyProperties;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.NotificationCompat;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

public class Social extends AppCompatActivity {

    private static final String KEY_NAME = "example_key";
    private static final int notification_id = 001;
    RelativeLayout lock_screen;
    NotificationCompat.Builder notification;
    SharedPreferences pinFile;
    EditText pin;
    TextView infoBar;
    Button enter_button;
    InputMethodManager inputMethodManager;
    ImageButton menu_button;
    String[] siteList;
    String[] siteLinkList;
    boolean[] checkedSites;
    ArrayList<Integer> userSites = new ArrayList<>();
    int TAB_COUNT = 3;
    TabFragment tab[] = new TabFragment[8];
    private EditText editText;
    private FingerprintManager fingerprintManager;
    private KeyguardManager keyguardManager;
    private KeyStore keyStore;
    private KeyGenerator keyGenerator;
    private Cipher cipher;
    private FingerprintManager.CryptoObject cryptoObject;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_social);

//        tab[0] = new TabFragment();
//        tab[1] = new TabFragment();
//        tab[2] = new TabFragment();

        editText = (EditText) findViewById(R.id.search_edit_text);

        final TabLayout tabLayout = (TabLayout) findViewById(R.id.tab_layout);
        tabLayout.addTab(tabLayout.newTab().setText("Facebook"));
        tabLayout.addTab(tabLayout.newTab().setText("Instagram"));
        tabLayout.addTab(tabLayout.newTab().setText("Twitter"));
        tabLayout.setTabGravity(TabLayout.GRAVITY_FILL);

        final ViewPager viewPager = (ViewPager) findViewById(R.id.pager);
//        final PagerAdapter adapter = new PagerAdapter (getSupportFragmentManager(), tabLayout.getTabCount());
        MyAdapter adapter = new MyAdapter(getSupportFragmentManager());
        viewPager.setAdapter(adapter);
        viewPager.setOffscreenPageLimit(TAB_COUNT);
        viewPager.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout));
        tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                viewPager.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        lock_screen = (RelativeLayout) findViewById(R.id.lock_screen);
        inputMethodManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);


        pin = (EditText) findViewById(R.id.pin);
        enter_button = (Button) findViewById(R.id.enter_button);
        infoBar = (TextView) findViewById(R.id.infoBar);
        pinFile = getSharedPreferences("pinFile", Context.MODE_PRIVATE);


        if ((pinFile.getString("pin", "")).equals("")) {
            infoBar.setText("Save a Pin to proceed");
            enter_button.setText("Save");
        } else {
            keyguardManager = (KeyguardManager) getSystemService(KEYGUARD_SERVICE);
            fingerprintManager = (FingerprintManager) getSystemService(FINGERPRINT_SERVICE);

            if (!keyguardManager.isKeyguardSecure()) {
                Toast.makeText(this, "Lock screen security not enabled in Settings", Toast.LENGTH_LONG).show();
                return;
            }

            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.USE_FINGERPRINT) != PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Fingerprint authentication permission not enabled", Toast.LENGTH_LONG).show();
                return;
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {
                // This happens when no fingerprints are registered.
                Toast.makeText(this, "Register at least one fingerprint in Settings", Toast.LENGTH_LONG).show();
                return;
            }

            if (!fingerprintManager.hasEnrolledFingerprints()) {

                // This happens when no fingerprints are registered.
                Toast.makeText(this,
                        "Register at least one fingerprint in Settings",
                        Toast.LENGTH_LONG).show();
                return;
            }

            generateKey();

            if (cipherInit()) {
                cryptoObject = new FingerprintManager.CryptoObject(cipher);
            }

            if (cipherInit()) {
                cryptoObject = new FingerprintManager.CryptoObject(cipher);
                FingerprintHandler helper = new FingerprintHandler(this);
                helper.startAuth(fingerprintManager, cryptoObject);
            }
        }

        menu_button = (ImageButton) findViewById(R.id.menu_button);

        siteList = getResources().getStringArray(R.array.shopping_sites);
        siteLinkList = getResources().getStringArray(R.array.shopping_sites_links);
        checkedSites = new boolean[siteList.length];

//        menu_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                AlertDialog.Builder mBuilder = new AlertDialog.Builder(Social.this);
//                mBuilder.setTitle("Shopping Sites");
//                mBuilder.setMultiChoiceItems(siteList, checkedSites, new DialogInterface.OnMultiChoiceClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int position, boolean isChecked) {
//                        if (isChecked) {
//                            userSites.add(position);
//                        } else {
//                            userSites.remove((Integer.valueOf(position)));
//                        }
//                    }
//                });
//
//                mBuilder.setCancelable(false);
//                mBuilder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int which) {
//
//                        viewPager.removeAllViews();
//                        tabLayout.removeAllTabs();
//
//                        final TabLayout tabLayout1 = (TabLayout) findViewById(R.id.tab_layout);
//                        TAB_COUNT = userSites.size();
//                        for (int i = 0; i < userSites.size(); i++) {
//                            tabLayout1.addTab(tabLayout1.newTab().setText(siteList[userSites.get(i)]));
//                        }
//                        tabLayout1.setTabGravity(TabLayout.GRAVITY_FILL);
//                        final ViewPager viewPager1 = (ViewPager) findViewById(R.id.pager);
//
//                        MyAdapter adapter1 = new MyAdapter(getSupportFragmentManager());
//                        viewPager1.setAdapter(adapter1);
//                        viewPager1.setOffscreenPageLimit(userSites.size());
//                        viewPager1.addOnPageChangeListener(new TabLayout.TabLayoutOnPageChangeListener(tabLayout1));
//                        tabLayout1.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
//                            @Override
//                            public void onTabSelected(TabLayout.Tab tab) {
//                                viewPager1.setCurrentItem(tab.getPosition());
//                            }
//
//                            @Override
//                            public void onTabUnselected(TabLayout.Tab tab) {
//
//                            }
//
//                            @Override
//                            public void onTabReselected(TabLayout.Tab tab) {
//
//                            }
//                        });
//
////                        for (int i = 0; i < userSites.size(); i++) {
////                            tab[i] = new TabFragment();
////                            tab[i].setUrl(siteLinkList[userSites.get(i)]);
////                            System.out.println(siteLinkList[userSites.get(i)]);
////                        }
//                    }
//                });
//
//                mBuilder.setNegativeButton("Dismiss", new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialogInterface, int i) {
//                        dialogInterface.dismiss();
//                    }
//                });
//
////                mBuilder.setNeutralButton("Clear all", new DialogInterface.OnClickListener() {
////                    @Override
////                    public void onClick(DialogInterface dialogInterface, int which) {
////                        for (int i = 0; i < checkedSites.length; i++) {
////                            checkedSites[i] = false;
////                            userSites.clear();
////                        }
////                    }
////                });
//
//                AlertDialog mDialog = mBuilder.create();
//                mDialog.show();
//            }
//        });

    }


    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage("Are you sure you want to exit?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Social.super.onBackPressed();
                    }
                })
                .setNegativeButton("No", null)
                .show();


    }

    public void onEnterClick(View view) {
        if (enter_button.getText().equals("Save")) {
            pinFile.edit().putString("pin", pin.getText().toString()).apply();
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
            ((RelativeLayout) findViewById(R.id.activity_social)).removeView(lock_screen);

        } else if (pinFile.getString("pin", "").equals(pin.getText().toString())) {
            ((RelativeLayout) findViewById(R.id.activity_social)).removeView(lock_screen);
            inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);

        } else {
            infoBar.setText("Wrong Pin");
        }
    }

    public void search(View view) {
        String flipkart_url = "https://www.flipkart.com/search?q=";
        String jabong_url = "http://www.jabong.com/find/";
        String amazon_url = "http://www.amazon.in/s/ref=nb_sb_noss_2?url=search-alias%3Daps&field-keywords=";


        //tab[0].webView = (WebView) findViewById(R.id.web_view_1);
//        WebView webView1 = (WebView) findViewById(R.id.web_view_1);
//        WebView webView2 = (WebView) findViewById(R.id.web_view_1);
//        WebView webView3 = (WebView) findViewById(R.id.web_view_1);

        tab[0].webView.loadUrl(flipkart_url + (editText.getText().toString()));
        tab[1].webView.loadUrl(jabong_url + (editText.getText().toString()));
        tab[2].webView.loadUrl(amazon_url + (editText.getText().toString()));
//        webView2.loadUrl(snapdeal_url + (editText.getText().toString()));
//        webView3.loadUrl(amazon_url + (editText.getText().toString()));

//        tab[0].load(flipkart_url + (editText.getText().toString()));
//        tab[1].load(snapdeal_url + (editText.getText().toString()));
//        tab[2].load(amazon_url + (editText.getText().toString()));

        inputMethodManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    protected void generateKey() {
        try {
            keyStore = KeyStore.getInstance("AndroidKeyStore");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore");
        } catch (NoSuchAlgorithmException | NoSuchProviderException e) {
            throw new RuntimeException("Failed to get KeyGenerator instance", e);
        }

        try {
            keyStore.load(null);
            keyGenerator.init(new
                    KeyGenParameterSpec.Builder(KEY_NAME, KeyProperties.PURPOSE_ENCRYPT | KeyProperties.PURPOSE_DECRYPT)
                    .setBlockModes(KeyProperties.BLOCK_MODE_CBC)
                    .setUserAuthenticationRequired(true)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_PKCS7)
                    .build());
            keyGenerator.generateKey();
        } catch (NoSuchAlgorithmException |
                InvalidAlgorithmParameterException
                | CertificateException | IOException e) {
            throw new RuntimeException(e);
        }
    }

    public boolean cipherInit() {
        try {
            cipher = Cipher.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES + "/"
                            + KeyProperties.BLOCK_MODE_CBC + "/"
                            + KeyProperties.ENCRYPTION_PADDING_PKCS7);
        } catch (NoSuchAlgorithmException |
                NoSuchPaddingException e) {
            throw new RuntimeException("Failed to get Cipher", e);
        }

        try {
            keyStore.load(null);
            SecretKey key = (SecretKey) keyStore.getKey(KEY_NAME, null);
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return true;
        } catch (KeyPermanentlyInvalidatedException e) {
            return false;
        } catch (KeyStoreException | CertificateException
                | UnrecoverableKeyException | IOException
                | NoSuchAlgorithmException | InvalidKeyException e) {
            throw new RuntimeException("Failed to init Cipher", e);
        }
    }

    public void removeLayout() {
        ((RelativeLayout) findViewById(R.id.activity_social))
                .removeView(lock_screen);
    }

    private class MyAdapter extends FragmentPagerAdapter {

        public MyAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public int getCount() {
            return TAB_COUNT;
        }

        @Override
        public Fragment getItem(int position) {

            if (userSites.size() <= 0) {

                switch (position) {
                    case 0:
                        tab[0] = new TabFragment();
                        tab[0].setUrl("https://www.facebook.com/");
                        return tab[0];
                    case 1:
                        tab[1] = new TabFragment();
                        tab[1].setUrl("https://www.instagram.com/");
                        return tab[1];
                    case 2:
                        tab[2] = new TabFragment();
                        tab[2].setUrl("https://twitter.com/");
                        return tab[2];
                    default:
                        return null;
                }
            } else {

                switch (position) {
                    case 0:
                    case 1:
                    case 3:
                    case 5:
                    case 7:
                    case 8:
                        tab[position] = new TabFragment();
                        if (!userSites.isEmpty())
                            tab[position].setUrl(siteLinkList[userSites.get(position)]);
                        else
                            tab[position].setUrl("https://www.flipkart.com/");
                        //System.out.println(siteLinkList[userSites.get(position)]);
                        return tab[position];

                    default:
                        return null;
                }

            }
//            tab[position] = new TabFragment();
//            tab[position].setUrl(siteLinkList[userSites.get(position)]);
//            return tab[position];
//            for (int i = 0; i < userSites.size(); i++) {
//                tab[i] = new TabFragment();
//                tab[i].setUrl(siteLinkList[userSites.get(i)]);
//                System.out.println(siteLinkList[userSites.get(i)]);
//            }

        }

    }

}