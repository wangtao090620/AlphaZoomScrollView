package module.scrollview;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import module.alphazoomscrollview.AlphaZoomScrollView;

public class MainActivity extends AppCompatActivity {

    private RecyclerView mRecyclerView;

    private FriendsAdapter mAdapter;
    private AlphaZoomScrollView mScrollView;
    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            setTranslucentStatus(true);
        }
    }

    public int getStatusBarHeight() {
        int result = 0;
        int resourceId = getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resourceId > 0) {
            result = getResources().getDimensionPixelSize(resourceId);
        }
        return result;
    }

    @TargetApi(19)
    private void setTranslucentStatus(boolean on) {
        Window win = getWindow();
        WindowManager.LayoutParams winParams = win.getAttributes();
        final int bits = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS;
        if (on) {
            winParams.flags |= bits;
        } else {
            winParams.flags &= ~bits;
        }
        win.setAttributes(winParams);
    }


    private void initView() {

        mScrollView = (AlphaZoomScrollView) findViewById(R.id.nestedScrollView);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycleView);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        mToolbar.getBackground().setAlpha(0);


        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);

        mAdapter = new FriendsAdapter(this);
        mRecyclerView.setAdapter(mAdapter);


        mScrollView.setIsParallax(true);
        mScrollView.setIsZoomEnable(true);
        mScrollView.setSensitive(1.5f);
        mScrollView.setZoomTime(500);

        mScrollView.setColorRed(147);
        mScrollView.setColorGreen(176);
        mScrollView.setColorBlue(170);


        mScrollView.setOnPullZoomListener(new AlphaZoomScrollView.OnPullZoomListener() {
            @Override
            public void onPullZoom(int originHeight, int currentHeight) {

            }

            @Override
            public void onZoomFinish() {

            }
        });

        mScrollView.setAlphaListener(new AlphaZoomScrollView.AlphaListener() {
            @Override
            public void toolbarAlphaChange(int alpha) {
                mToolbar.setBackgroundColor(alpha);
            }
        });

    }


    private class FriendsAdapter extends RecyclerView.Adapter<FriendsAdapter.FriendsHolder> {

        private List<String> mData = new ArrayList<>();
        private Context mContext;

        FriendsAdapter(Context context) {
            mContext = context;
            for (int i = 0; i < 30; i++) { //warning oom 数据过大会造成OOM
                mData.add("今天天气不错");
            }
        }

        @Override
        public FriendsHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new FriendsHolder(LayoutInflater.from(mContext).inflate(R.layout.holder_friend, parent, false));
        }

        @Override
        public void onBindViewHolder(FriendsHolder holder, int position) {
            holder.mText.setText(mData.get(position));
        }


        @Override
        public int getItemCount() {
            return mData.size();
        }

        class FriendsHolder extends RecyclerView.ViewHolder {

            private final TextView mText;

            FriendsHolder(View itemView) {
                super(itemView);
                mText = (TextView) itemView.findViewById(R.id.text);
            }
        }
    }

}
