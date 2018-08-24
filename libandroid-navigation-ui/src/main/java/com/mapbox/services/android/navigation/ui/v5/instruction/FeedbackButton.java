package com.mapbox.services.android.navigation.ui.v5.instruction;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.util.AttributeSet;
import android.view.View;

import com.mapbox.services.android.navigation.ui.v5.NavigationViewModel;
import com.mapbox.services.android.navigation.ui.v5.R;
import com.mapbox.services.android.navigation.ui.v5.feedback.FeedbackBottomSheet;
import com.mapbox.services.android.navigation.ui.v5.feedback.FeedbackBottomSheetListener;
import com.mapbox.services.android.navigation.v5.navigation.NavigationConstants;
import com.mapbox.services.android.navigation.v5.navigation.metrics.FeedbackEvent;

import java.util.ArrayList;
import java.util.List;

import timber.log.Timber;

public class FeedbackButton extends ConstraintLayout implements MapButton {
  private FloatingActionButton feedbackFab;
  private NavigationViewModel navigationViewModel;
  private FeedbackBottomSheetListener feedbackBottomSheetListener;
  private List<OnClickListener> onClickListeners;


  public FeedbackButton(Context context) {
    this(context, null);
    initialize(context);
  }

  public FeedbackButton(Context context, AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public FeedbackButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context);
  }

  /**
   * Sets feedbackBottomSheetListener. Must be called for proper initialization.
   *
   * @param feedbackBottomSheetListener to set
   */
  public void setFeedbackBottomSheetListener(FeedbackBottomSheetListener feedbackBottomSheetListener) {
    this.feedbackBottomSheetListener = feedbackBottomSheetListener;
  }

  private void initialize(Context context) {
    onClickListeners = new ArrayList<>();
    inflate(context, R.layout.feedback_button_layout, this);
    bind();
  }

  private void bind() {
    feedbackFab = findViewById(R.id.feedbackFab);
  }

  private void initializeClickListener() {
    feedbackFab.setVisibility(VISIBLE);
    feedbackFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        navigationViewModel.recordFeedback(FeedbackEvent.FEEDBACK_SOURCE_UI);
        showFeedbackBottomSheet();

        for (OnClickListener onClickListener : onClickListeners) {
          onClickListener.onClick(view);
        }
      }
    });
  }

  private void showFeedbackBottomSheet() {
    FragmentManager fragmentManager = obtainSupportFragmentManger();
    if (fragmentManager != null) {
      long duration = NavigationConstants.FEEDBACK_BOTTOM_SHEET_DURATION;
      FeedbackBottomSheet.newInstance(feedbackBottomSheetListener, duration)
        .show(fragmentManager, FeedbackBottomSheet.TAG);
      navigationViewModel.isFeedbackShowing.setValue(true);
    }
  }

  @Nullable
  private FragmentManager obtainSupportFragmentManger() {
    try {
      return ((FragmentActivity) getContext()).getSupportFragmentManager();
    } catch (ClassCastException exception) {
      Timber.e(exception);
      return null;
    }
  }

  @Override
  public void subscribe(NavigationViewModel navigationViewModel) {
    this.navigationViewModel = navigationViewModel;
    initializeClickListener();
  }

  @Override
  public void addOnClickListener(OnClickListener onClickListener) {
    onClickListeners.add(onClickListener);
  }
}
