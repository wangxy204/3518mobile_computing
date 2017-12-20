package com.bignerdranch.android.criminalintent;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;

import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.FileProvider;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.List;
import java.util.UUID;


import static android.widget.CompoundButton.*;

public class CrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int ALTER_PHOTO = 3;

    private Crime mCrime;
    private File mPhotoFile;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckbox;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private ImageView mPhotoView;
    private ImageView extraPhoto1;
    private ImageView extraPhoto2;
    private ImageView extraPhoto3;
    private ImageView c_photo;
    private int i = 1;




    public static CrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        CrimeFragment fragment = new CrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_crime, container, false);

        mTitleField = (EditText) v.findViewById(R.id.crime_title);
        mTitleField.setText(mCrime.getTitle());
        mTitleField.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                mCrime.setTitle(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        mDateButton = (Button) v.findViewById(R.id.crime_date);
        updateDate();
        mDateButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mCrime.getDate());
                dialog.setTargetFragment(CrimeFragment.this, REQUEST_DATE);
                dialog.show(manager, DIALOG_DATE);
            }
        });

        mSolvedCheckbox = (CheckBox) v.findViewById(R.id.crime_solved);
        mSolvedCheckbox.setChecked(mCrime.isSolved());
        mSolvedCheckbox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, 
                    boolean isChecked) {
                mCrime.setSolved(isChecked);
            }
        });

        mReportButton = (Button) v.findViewById(R.id.crime_report);
        mReportButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_SEND);
                i.setType("text/plain");
                i.putExtra(Intent.EXTRA_TEXT, getCrimeReport());
                i.putExtra(Intent.EXTRA_SUBJECT,
                        getString(R.string.crime_report_subject));
                i = Intent.createChooser(i, getString(R.string.send_report));
                startActivity(i);
            }
        });

        final Intent pickContact = new Intent(Intent.ACTION_PICK,
                ContactsContract.Contacts.CONTENT_URI);
        mSuspectButton = (Button) v.findViewById(R.id.crime_suspect);
        mSuspectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                startActivityForResult(pickContact, REQUEST_CONTACT);
            }
        });
        if (mCrime.getSuspect() != null) {
            mSuspectButton.setText(mCrime.getSuspect());
        }

        PackageManager packageManager = getActivity().getPackageManager();
        if (packageManager.resolveActivity(pickContact,
                PackageManager.MATCH_DEFAULT_ONLY) == null) {
            mSuspectButton.setEnabled(false);
        }




        //take photo part
        mPhotoButton = (ImageButton) v.findViewById(R.id.crime_camera);
        final Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto = mPhotoFile != null &&
                captureImage.resolveActivity(packageManager) != null;
        mPhotoButton.setEnabled(canTakePhoto);


        mPhotoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
                //save the image when user click the image button
                String name = makeName();

                i = 1;
                mPhotoView = (ImageView) getActivity().findViewById(R.id.crime_photo);
                if(mPhotoView.getDrawable() != null){
                    i=2;
                    Toast.makeText(getActivity(),"LOL",Toast.LENGTH_SHORT).show();
                    mPhotoView = (ImageView) getActivity().findViewById(R.id.image2);
                    if (mPhotoView.getDrawable() != null){
                        i=3;
                        mPhotoView = (ImageView) getActivity().findViewById(R.id.image3);
                        if (mPhotoView.getDrawable() != null){
                            i=4;
                            mPhotoView = (ImageView) getActivity().findViewById(R.id.image4);
                            mPhotoButton.setEnabled(false); // the photo button will be disabled after the last picture is taken
                        }
                    }
                }
                Uri uriSavedImage=Uri.fromFile(new File("/sdcard/DCIM/Camera/"+name+i+".png"));
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);


                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }


                startActivityForResult(captureImage, REQUEST_PHOTO);
            }


        });




        extraPhoto1 = (ImageView) v.findViewById(R.id.image2);
        final Intent captureImage1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto1 = mPhotoFile != null &&
                captureImage1.resolveActivity(packageManager) != null;
        extraPhoto1.setEnabled(canTakePhoto1);
        extraPhoto1.setOnTouchListener(new OnSwipeTouchListener(){
            @Override
            public boolean onSwipeTop() {
                extraPhoto1.setImageDrawable(null);
                return true;
            }

            @Override
            public boolean onSwipeBottom() {
                extraPhoto1.setImageDrawable(null);
                return true;
            }
        });
        extraPhoto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);
                captureImage1.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                //save the pic
                String name = makeName();
                Uri uriSavedImage=Uri.fromFile(new File("/sdcard/DCIM/Camera/"+name+"1"+".png"));
                captureImage1.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage1,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                mPhotoView = extraPhoto1;
                startActivityForResult(captureImage1, ALTER_PHOTO);
            }

        });



        extraPhoto2 = (ImageView) v.findViewById(R.id.image3);
        final Intent captureImage2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto2 = mPhotoFile != null &&
                captureImage2.resolveActivity(packageManager) != null;
        extraPhoto2.setEnabled(canTakePhoto2);
        extraPhoto2.setOnTouchListener(new OnSwipeTouchListener(){
            @Override
            public boolean onSwipeTop() {
                extraPhoto2.setImageDrawable(null);
                return true;
            }

            @Override
            public boolean onSwipeBottom() {
                extraPhoto2.setImageDrawable(null);
                return true;
            }
        });
        extraPhoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);
                captureImage2.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                //save the pic
                String name = makeName();
                Uri uriSavedImage=Uri.fromFile(new File("/sdcard/DCIM/Camera/"+name+"2"+".png"));
                captureImage2.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage2,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                mPhotoView = extraPhoto2;
                startActivityForResult(captureImage2, ALTER_PHOTO);


            }

        });


        extraPhoto3 = (ImageView) v.findViewById(R.id.image4);
        final Intent captureImage3 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto3 = mPhotoFile != null &&
                captureImage2.resolveActivity(packageManager) != null;
        extraPhoto3.setEnabled(canTakePhoto3);
        extraPhoto3.setOnTouchListener(new OnSwipeTouchListener(){
            @Override
            public boolean onSwipeTop() {
                extraPhoto3.setImageDrawable(null);
                return true;
            }

            @Override
            public boolean onSwipeBottom() {
                extraPhoto3.setImageDrawable(null);
                return true;
            }
        });
        extraPhoto3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);
                captureImage3.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                //save the pic
                String name = makeName();
                Uri uriSavedImage=Uri.fromFile(new File("/sdcard/DCIM/Camera/"+name+"3"+".png"));
                captureImage3.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage3,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }
                mPhotoView = extraPhoto3;
                startActivityForResult(captureImage3, ALTER_PHOTO);
            }


        });

        c_photo = (ImageView) v.findViewById(R.id.crime_photo);
        final Intent captureImage4 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        boolean canTakePhoto4 = mPhotoFile != null &&
                captureImage4.resolveActivity(packageManager) != null;
        c_photo.setEnabled(canTakePhoto4);
        c_photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        mPhotoFile);
                captureImage4.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                //here to save
                String name = makeName();

                Uri uriSavedImage=Uri.fromFile(new File("/sdcard/DCIM/Camera/"+name+"4"+".png"));
                captureImage4.putExtra(MediaStore.EXTRA_OUTPUT, uriSavedImage);

//                File imagefolder = new File(Environment.getExternalStorageDirectory(),"MyImages");
//                imagefolder.mkdirs();
//                File image = new File(imagefolder,"image.jpg");
//                Uri savedImg = Uri.fromFile(image);
//                captureImage4.putExtra(MediaStore.EXTRA_OUTPUT,savedImg);
//

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage4,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }

                mPhotoView = c_photo;
                startActivityForResult(captureImage4, ALTER_PHOTO);
            }

        });







        mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);
        updatePhotoView();

        return v;
    }


    private String makeName() {

        return "IMG" + mCrime.getId();
    }



    @Override
    public void onPause() {
        super.onPause();

        CrimeLab.get(getActivity())
                .updateCrime(mCrime);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        if (requestCode == REQUEST_DATE) {
            Date date = (Date) data
                    .getSerializableExtra(DatePickerFragment.EXTRA_DATE);
            mCrime.setDate(date);
            updateDate();
        } else if (requestCode == REQUEST_CONTACT && data != null) {
            Uri contactUri = data.getData();
            // Specify which fields you want your query to return
            // values for.
            String[] queryFields = new String[]{
                    ContactsContract.Contacts.DISPLAY_NAME
            };
            // Perform your query - the contactUri is like a "where"
            // clause here
            Cursor c = getActivity().getContentResolver()
                    .query(contactUri, queryFields, null, null, null);
            try {
                // Double-check that you actually got results
                if (c.getCount() == 0) {
                    return;
                }
                // Pull out the first column of the first row of data -
                // that is your suspect's name.
                c.moveToFirst();
                String suspect = c.getString(0);
                mCrime.setSuspect(suspect);
                mSuspectButton.setText(suspect);
            } finally {
                c.close();
            }
        } else if (requestCode == REQUEST_PHOTO) {
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    mPhotoFile);

            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            i = 1;
            mPhotoView = (ImageView) getActivity().findViewById(R.id.crime_photo);
            if(mPhotoView.getDrawable() != null){
                i=2;
                Toast.makeText(getActivity(),"LOL",Toast.LENGTH_SHORT).show();
                mPhotoView = (ImageView) getActivity().findViewById(R.id.image2);
                if (mPhotoView.getDrawable() != null){
                    i=3;
                    mPhotoView = (ImageView) getActivity().findViewById(R.id.image3);
                    if (mPhotoView.getDrawable() != null){
                        i=4;
                        mPhotoView = (ImageView) getActivity().findViewById(R.id.image4);
                        mPhotoButton.setEnabled(false); // the photo button will be disabled after the last picture is taken
                    }
                }
            }

            //mPhotoFile = new File("/sdcard/DCIM/Camera/"+makeName()+i+".png");




            updatePhotoView();
        }
        else if(requestCode == ALTER_PHOTO){
            updatePhotoView();

        }
    }

    public void copyFile(File fromFile,File toFile) throws IOException {
        FileInputStream ins = new FileInputStream(fromFile);
        FileOutputStream out = new FileOutputStream(toFile);
        byte[] b = new byte[1024];
        int n=0;
        while((n=ins.read(b))!=-1){
            out.write(b, 0, n);
        }

        ins.close();
        out.close();
    }



    private void updateDate() {
        mDateButton.setText(mCrime.getDate().toString());
    }

    private String getCrimeReport() {
        String solvedString = null;
        if (mCrime.isSolved()) {
            solvedString = getString(R.string.crime_report_solved);
        } else {
            solvedString = getString(R.string.crime_report_unsolved);
        }
        String dateFormat = "EEE, MMM dd";
        String dateString = DateFormat.format(dateFormat, mCrime.getDate()).toString();
        String suspect = mCrime.getSuspect();
        if (suspect == null) {
            suspect = getString(R.string.crime_report_no_suspect);
        } else {
            suspect = getString(R.string.crime_report_suspect, suspect);
        }
        String report = getString(R.string.crime_report,
                mCrime.getTitle(), dateString, solvedString, suspect);
        return report;
    }

//    private void updatePhotoView() {
//        if (mPhotoFile == null || !mPhotoFile.exists()) {
//            mPhotoView.setImageDrawable(null);
//        } else {
//            Bitmap bitmap = PictureUtils.getScaledBitmap(
//                    mPhotoFile.getPath(), getActivity());
//            mPhotoView.setImageBitmap(bitmap);
//        }
//    }

    private void getPermission(){
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {

            // Should we show an explanation?
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(),
                    Manifest.permission.READ_CONTACTS)) {

                // Show an expanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        666);

                // MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
                // app-defined int constant. The callback method gets the
                // result of the request.
            }
        }
    }
    private void updatePhotoView() {
        getPermission();
        Log.d("fu","update");
        File save = new File("/sdcard/DCIM/Camera/"+makeName()+(i)+".png");
        if(save.exists()){
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    save.getPath(), getActivity());
            mPhotoView.setImageBitmap(bitmap);
        }
    }



    public class OnSwipeTouchListener implements OnTouchListener {

        private final GestureDetector gestureDetector = new GestureDetector(new GestureListener());

        public boolean onTouch(final View v, final MotionEvent event) {
            return gestureDetector.onTouchEvent(event);
        }

        private final class GestureListener extends GestureDetector.SimpleOnGestureListener {

            private static final int SWIPE_THRESHOLD = 100;
            private static final int SWIPE_VELOCITY_THRESHOLD = 100;


            @Override
            public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
                boolean result = false;
                try {
                    float diffY = e2.getY() - e1.getY();
                    float diffX = e2.getX() - e1.getX();
                    if (Math.abs(diffX) > Math.abs(diffY)) {
                        if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffX > 0) {
                                result = onSwipeRight();
                            } else {
                                result = onSwipeLeft();
                            }
                        }
                    } else {
                        if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
                            if (diffY > 0) {
                                result = onSwipeBottom();
                            } else {
                                result = onSwipeTop();
                            }
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
                return result;
            }
        }

        public boolean onSwipeRight() {
            return false;
        }

        public boolean onSwipeLeft() {
            return false;
        }

        public boolean onSwipeTop() {
            return false;
        }

        public boolean onSwipeBottom() {
            return false;
        }
    }


}
