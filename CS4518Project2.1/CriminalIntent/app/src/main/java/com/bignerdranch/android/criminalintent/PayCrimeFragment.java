package com.bignerdranch.android.criminalintent;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.FileProvider;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.File;
import java.security.PrivateKey;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import static android.widget.CompoundButton.OnCheckedChangeListener;
import static android.widget.CompoundButton.OnClickListener;

public class PayCrimeFragment extends Fragment {

    private static final String ARG_CRIME_ID = "crime_id";
    private static final String DIALOG_DATE = "DialogDate";

    private static final int REQUEST_DATE = 0;
    private static final int REQUEST_CONTACT = 1;
    private static final int REQUEST_PHOTO = 2;
    private static final int ALTER_PHOTO = 3;

    private Crime mCrime;
    private EditText mTitleField;
    private Button mDateButton;
    private CheckBox mSolvedCheckbox;
    private Button mReportButton;
    private Button mSuspectButton;
    private ImageButton mPhotoButton;
    private List<File> mPhotoFiles;
    //private List<ImageView>mPhotoViews;
    private RecyclerView mImageRecyclerView;
    private ImageAdapter mAdapter;
    private ImageView lefttop;

    private void getPhotoFile(){
        String parent = CrimeLab.get(getActivity()).dirname();
        int index = 0;
        mPhotoFiles = new ArrayList<File>();
        File image = new File(parent,"IMG_" + mCrime.getId().toString() + index + ".jpg");
        while (image.exists()){
            mPhotoFiles.add(image);
            index ++;
            image = new File(parent,"IMG_" + mCrime.getId().toString() + index + ".jpg");
        }
    }


    public static PayCrimeFragment newInstance(UUID crimeId) {
        Bundle args = new Bundle();
        args.putSerializable(ARG_CRIME_ID, crimeId);

        PayCrimeFragment fragment = new PayCrimeFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        UUID crimeId = (UUID) getArguments().getSerializable(ARG_CRIME_ID);
        mCrime = CrimeLab.get(getActivity()).getCrime(crimeId);
        getPhotoFile();
        //mPhotoFile = CrimeLab.get(getActivity()).getPhotoFile(mCrime);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_image_recycler,container,false);
        View v = inflater.inflate(R.layout.fragment_pay_crime, container, false);
        mImageRecyclerView = (RecyclerView) view
                .findViewById(R.id.imagerecycle);
        mImageRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity(),LinearLayoutManager.HORIZONTAL,false));
        LinearLayout layout = (LinearLayout) v.findViewById(R.id.r_layout);
        lefttop = (ImageView) v.findViewById(R.id.crime_photo);
        layout.addView(view);



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
        mDateButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentManager manager = getFragmentManager();
                DatePickerFragment dialog = DatePickerFragment
                        .newInstance(mCrime.getDate());
                dialog.setTargetFragment(PayCrimeFragment.this, REQUEST_DATE);
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
        mReportButton.setOnClickListener(new OnClickListener() {
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
        mSuspectButton.setOnClickListener(new OnClickListener() {
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
//        boolean canTakePhoto = mPhotoFile != null &&
//                captureImage.resolveActivity(packageManager) != null;
       // mPhotoButton.setEnabled(canTakePhoto);

        mPhotoButton.setOnClickListener(new OnClickListener() {
            @Override

            public void onClick(View v) {
                File image = new File(CrimeLab.get(getActivity()).dirname(),"IMG_" +mCrime.getId().toString()+mPhotoFiles.size()+ ".jpg");
                Uri uri = FileProvider.getUriForFile(getActivity(),
                        "com.bignerdranch.android.criminalintent.fileprovider",
                        image);
                mPhotoFiles.add(image);
                captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);

                List<ResolveInfo> cameraActivities = getActivity()
                        .getPackageManager().queryIntentActivities(captureImage,
                                PackageManager.MATCH_DEFAULT_ONLY);

                for (ResolveInfo activity : cameraActivities) {
                    getActivity().grantUriPermission(activity.activityInfo.packageName,
                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                }



                startActivityForResult(captureImage, REQUEST_PHOTO);
            }
            private String makeName() {
                SimpleDateFormat date = new SimpleDateFormat("yyyyMMdd_HHmmss");
                String time = date.format(new Date());
                return "IMG" + time + ".jpg";
            }

        });





        //extraPhoto1 = (ImageView) v.findViewById(R.id.image2);
        //final Intent captureImage1 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        //boolean canTakePhoto1 = mPhotoFile != null &&
                //captureImage1.resolveActivity(packageManager) != null;
        //extraPhoto1.setEnabled(canTakePhoto1);
       //extraPhoto1.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = FileProvider.getUriForFile(getActivity(),
//                        "com.bignerdranch.android.criminalintent.fileprovider",
//                        mPhotoFile);
//                captureImage1.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//
//                List<ResolveInfo> cameraActivities = getActivity()
//                        .getPackageManager().queryIntentActivities(captureImage1,
//                                PackageManager.MATCH_DEFAULT_ONLY);
//
//                for (ResolveInfo activity : cameraActivities) {
//                    getActivity().grantUriPermission(activity.activityInfo.packageName,
//                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                }
//
//                mPhotoView = extraPhoto1;
//                startActivityForResult(captureImage1, ALTER_PHOTO);
//            }
//        });



//        extraPhoto2 = (ImageView) v.findViewById(R.id.image3);
//        final Intent captureImage2 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        boolean canTakePhoto2 = mPhotoFile != null &&
//                captureImage2.resolveActivity(packageManager) != null;
//        extraPhoto2.setEnabled(canTakePhoto2);
//        extraPhoto2.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = FileProvider.getUriForFile(getActivity(),
//                        "com.bignerdranch.android.criminalintent.fileprovider",
//                        mPhotoFile);
//                captureImage2.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//
//                List<ResolveInfo> cameraActivities = getActivity()
//                        .getPackageManager().queryIntentActivities(captureImage2,
//                                PackageManager.MATCH_DEFAULT_ONLY);
//
//                for (ResolveInfo activity : cameraActivities) {
//                    getActivity().grantUriPermission(activity.activityInfo.packageName,
//                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                }
//                mPhotoView = extraPhoto2;
//                startActivityForResult(captureImage2, ALTER_PHOTO);
//            }
//        });


//        extraPhoto3 = (ImageView) v.findViewById(R.id.image4);
//        final Intent captureImage3 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        boolean canTakePhoto3 = mPhotoFile != null &&
//                captureImage2.resolveActivity(packageManager) != null;
//        extraPhoto3.setEnabled(canTakePhoto3);
//        extraPhoto3.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = FileProvider.getUriForFile(getActivity(),
//                        "com.bignerdranch.android.criminalintent.fileprovider",
//                        mPhotoFile);
//                captureImage3.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//
//                List<ResolveInfo> cameraActivities = getActivity()
//                        .getPackageManager().queryIntentActivities(captureImage3,
//                                PackageManager.MATCH_DEFAULT_ONLY);
//
//                for (ResolveInfo activity : cameraActivities) {
//                    getActivity().grantUriPermission(activity.activityInfo.packageName,
//                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                }
//                mPhotoView = extraPhoto3;
//                startActivityForResult(captureImage3, ALTER_PHOTO);
//            }
//        });

//        c_photo = (ImageView) v.findViewById(R.id.crime_photo);
//        final Intent captureImage4 = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//        boolean canTakePhoto4 = mPhotoFile != null &&
//                captureImage4.resolveActivity(packageManager) != null;
//        c_photo.setEnabled(canTakePhoto4);
//        c_photo.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Uri uri = FileProvider.getUriForFile(getActivity(),
//                        "com.bignerdranch.android.criminalintent.fileprovider",
//                        mPhotoFile);
//                captureImage4.putExtra(MediaStore.EXTRA_OUTPUT, uri);
//
//                List<ResolveInfo> cameraActivities = getActivity()
//                        .getPackageManager().queryIntentActivities(captureImage4,
//                                PackageManager.MATCH_DEFAULT_ONLY);
//
//                for (ResolveInfo activity : cameraActivities) {
//                    getActivity().grantUriPermission(activity.activityInfo.packageName,
//                            uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
//                }
//                mPhotoView = c_photo;
//                startActivityForResult(captureImage4, ALTER_PHOTO);
//            }
//        });









        //mPhotoView = (ImageView) v.findViewById(R.id.crime_photo);

        updatePhotoViews();

        return v;
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
                    mPhotoFiles.get(mPhotoFiles.size()-1));

            getActivity().revokeUriPermission(uri,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

//            mPhotoView = (ImageView) getActivity().findViewById(R.id.crime_photo);
//            if(mPhotoView.getDrawable() != null){
//                Toast.makeText(getActivity(),"LOL",Toast.LENGTH_SHORT).show();
//                mPhotoView = (ImageView) getActivity().findViewById(R.id.image2);
//                if (mPhotoView.getDrawable() != null){
//                    mPhotoView = (ImageView) getActivity().findViewById(R.id.image3);
//                    if (mPhotoView.getDrawable() != null){
//                        mPhotoView = (ImageView) getActivity().findViewById(R.id.image4);
//                        mPhotoButton.setEnabled(false); // the photo button will be disabled after the last picture is taken
//                    }
//                }
//            }
            updatePhotoViews();
        }
        else if(requestCode == ALTER_PHOTO){
            updatePhotoViews();
        }
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


    private class ImageHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private File mImage;
        private ImageView imageView;

        public ImageHolder(LayoutInflater inflater, ViewGroup parent) {
            super(inflater.inflate(R.layout.list_item_img, parent, false));
            itemView.setOnClickListener(this);

            imageView = (ImageView) itemView.findViewById(R.id.singeimg);

        }

        public void bind(File image) {
            mImage = image;
            if (mImage == null || !mImage.exists()) {
                imageView.setImageDrawable(null);
            } else {
                Bitmap bitmap = PictureUtils.getScaledBitmap(
                        mImage.getPath(), getActivity());
                imageView.setImageBitmap(bitmap);
            }

        }

        @Override
        public void onClick(View view) {
            Intent captureImage = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            //Uri uri = FileProvider.getUriForFile((getActivity(),"com.bignerdranch.android.criminalintent.fileprovider",mPhotoFiles.get(getAdapterPosition()));
            Uri uri = FileProvider.getUriForFile(getActivity(),
                    "com.bignerdranch.android.criminalintent.fileprovider",
                    mPhotoFiles.get(getAdapterPosition()));
            captureImage.putExtra(MediaStore.EXTRA_OUTPUT, uri);
            List<ResolveInfo> camera = getActivity().getPackageManager().queryIntentActivities(captureImage, PackageManager.MATCH_DEFAULT_ONLY);
            for(ResolveInfo activity : camera){
                getActivity().grantUriPermission(activity.activityInfo.packageName, uri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            }
            startActivityForResult(captureImage,ALTER_PHOTO);
        }
    }


    private class ImageAdapter extends RecyclerView.Adapter<ImageHolder> {

        private List<File> imagelist;

        public ImageAdapter(List<File> imageslist) {
            imagelist = imageslist;
        }

        @Override
        public ImageHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            LayoutInflater layoutInflater = LayoutInflater.from(getActivity());
            return new ImageHolder(layoutInflater, parent);
        }

        @Override
        public void onBindViewHolder(ImageHolder holder, int position) {
            File image = imagelist.get(position);
            holder.bind(image);
        }

        @Override
        public int getItemCount() {
            return imagelist.size();
        }

        public void setImagelist(List<File> imageslist) {
            imagelist = imageslist;
        }
    }

    private void updatePhotoViews() {
        if (mPhotoFiles.size() > 0){

            updatePhotoView(lefttop, mPhotoFiles.get(mPhotoFiles.size()-1));
        }
        if (mAdapter == null) {
            mAdapter = new ImageAdapter(mPhotoFiles);
            mImageRecyclerView.setAdapter(mAdapter);
        } else {
            mAdapter.setImagelist(mPhotoFiles);
            mAdapter.notifyDataSetChanged();
        }
    }
    private void updatePhotoView(ImageView view, File image) {
        if (image == null || !image.exists()) {
            view.setImageDrawable(null);
        } else {
            Bitmap bitmap = PictureUtils.getScaledBitmap(
                    image.getPath(), getActivity());
            view.setImageBitmap(bitmap);
        }
    }
}
