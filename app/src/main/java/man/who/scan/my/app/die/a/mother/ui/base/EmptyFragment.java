package man.who.scan.my.app.die.a.mother.ui.base;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import man.who.scan.my.app.die.a.mother.R;

public class EmptyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.base_empty, container, false);
        return view;
    }


    @Override
    public void onPause() {
        super.onPause();
    }
}
