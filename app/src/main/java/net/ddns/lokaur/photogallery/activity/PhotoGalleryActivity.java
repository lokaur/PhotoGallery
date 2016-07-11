package net.ddns.lokaur.photogallery.activity;

import android.support.v4.app.Fragment;

import net.ddns.lokaur.photogallery.fragment.PhotoGalleryFragment;

public class PhotoGalleryActivity extends SingleFragmentActivity {

    @Override
    protected Fragment createFragment() {
        return PhotoGalleryFragment.newInstance();
    }
}
