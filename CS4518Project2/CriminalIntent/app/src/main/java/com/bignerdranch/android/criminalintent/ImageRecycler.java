package com.bignerdranch.android.criminalintent;


import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.io.File;
import java.util.List;


/**
 * A simple {@link Fragment} subclass.
 */
public class ImageRecycler extends Fragment {

    private RecyclerView mImageRecyclerView;
    private ImageAdapter mAdapter;


    public ImageRecycler() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_image_recycler, container, false);
        mImageRecyclerView = (RecyclerView) view
                .findViewById(R.id.imagerecycle);
        mImageRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        return view;


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
//            Intent intent = CrimePagerActivity.newIntent(getActivity(), mCrime.getId());
//            intent.putExtra("pay", ((CrimeListActivity)getActivity()).checkpay());
//            startActivity(intent);
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



}
