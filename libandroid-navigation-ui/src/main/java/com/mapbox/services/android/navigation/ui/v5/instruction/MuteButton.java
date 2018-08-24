package com.mapbox.services.android.navigation.ui.v5.instruction;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.graphics.drawable.DrawableCompat;
import android.util.AttributeSet;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.DecelerateInterpolator;
import android.widget.TextView;

import com.mapbox.services.android.navigation.ui.v5.NavigationViewModel;
import com.mapbox.services.android.navigation.ui.v5.R;
import com.mapbox.services.android.navigation.ui.v5.ThemeSwitcher;

import java.util.ArrayList;
import java.util.List;

public class MuteButton extends ConstraintLayout implements MapButton {
  FloatingActionButton soundFab;
  private boolean isMuted;
  private TextView soundChipText;
  private AnimationSet fadeInSlowOut;
  private NavigationViewModel navigationViewModel;
  private List<OnClickListener> onClickListeners;

  public MuteButton(Context context) {
    this(context, null);
  }

  public MuteButton(Context context, AttributeSet attrs) {
    this(context, attrs, -1);
  }

  public MuteButton(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    initialize(context);
  }

  private void initialize(Context context) {
    onClickListeners = new ArrayList<>();
    inflate(context, R.layout.sound_layout, this);
    bind();
    initializeSoundChipColor();
    initializeAnimations();
  }

  private void initializeAnimations() {
    Animation fadeIn = new AlphaAnimation(0, 1);
    fadeIn.setInterpolator(new DecelerateInterpolator());
    fadeIn.setDuration(300);

    Animation fadeOut = new AlphaAnimation(1, 0);
    fadeOut.setInterpolator(new AccelerateInterpolator());
    fadeOut.setStartOffset(1000);
    fadeOut.setDuration(1000);

    fadeInSlowOut = new AnimationSet(false);
    fadeInSlowOut.addAnimation(fadeIn);
    fadeInSlowOut.addAnimation(fadeOut);
  }

  private void bind() {
    soundFab = findViewById(R.id.soundFab);
    soundChipText = findViewById(R.id.soundText);
  }

  private void initializeSoundChipColor() {
    // Sound chip text - primary
    int navigationViewPrimaryColor = ThemeSwitcher.retrieveThemeColor(getContext(),
      R.attr.navigationViewPrimary);

    Drawable soundChipBackground = DrawableCompat.wrap(soundChipText.getBackground()).mutate();
    DrawableCompat.setTint(soundChipBackground, navigationViewPrimaryColor);
  }

  /**
   * Sets up mute UI event.
   * <p>
   * Shows chip with "Muted" text.
   * Changes sound {@link FloatingActionButton}
   * {@link android.graphics.drawable.Drawable} to denote sound is off.
   * <p>
   * Sets private state variable to true (muted)
   *
   * @return true, view is in muted state
   */
  private boolean mute() {
    isMuted = true;
    setSoundChipText(getContext().getString(R.string.muted));
    showSoundChip();
    soundFabOff();
    return isMuted;
  }

  /**
   * Sets up unmuted UI event.
   * <p>
   * Shows chip with "Unmuted" text.
   * Changes sound {@link FloatingActionButton}
   * {@link android.graphics.drawable.Drawable} to denote sound is on.
   * <p>
   * Sets private state variable to false (unmuted)
   *
   * @return false, view is in unmuted state
   */
  private boolean unmute() {
    isMuted = false;
    setSoundChipText(getContext().getString(R.string.unmuted));
    showSoundChip();
    soundFabOn();
    return isMuted;
  }

  /**
   * Sets {@link TextView} inside of chip view.
   *
   * @param text to be displayed in chip view ("Muted"/"Umuted")
   */
  private void setSoundChipText(String text) {
    soundChipText.setText(text);
  }

  /**
   * Shows and then hides the sound chip using {@link AnimationSet}
   */
  private void showSoundChip() {
    soundChipText.startAnimation(fadeInSlowOut);
  }

  /**
   * Changes sound {@link FloatingActionButton}
   * {@link android.graphics.drawable.Drawable} to denote sound is off.
   */
  private void soundFabOff() {
    soundFab.setImageResource(R.drawable.ic_sound_off);
  }

  /**
   * Changes sound {@link FloatingActionButton}
   * {@link android.graphics.drawable.Drawable} to denote sound is on.
   */
  private void soundFabOn() {
    soundFab.setImageResource(R.drawable.ic_sound_on);
  }

  /**
   * Will toggle the view between muted and unmuted states.
   *
   * @return boolean true if muted, false if not
   * @since 0.6.0
   */
  public boolean toggleMute() {
    return isMuted ? unmute() : mute();
  }

  /**
   * Subscribes to a {@link NavigationViewModel} for
   * updates from {@link android.arch.lifecycle.LiveData}.
   * <p>
   * Updates all views with fresh data / shows &amp; hides re-route state.
   *
   * @param navigationViewModel to which this View is subscribing
   * @since 0.6.2
   */
  @Override
  public void subscribe(NavigationViewModel navigationViewModel) {
    this.navigationViewModel = navigationViewModel;
    initializeClickListener();
  }

  @Override
  public void addOnClickListener(OnClickListener onClickListener) {
    onClickListeners.add(onClickListener);
  }

  private void initializeClickListener() {
    soundFab.setVisibility(VISIBLE);
    soundFab.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View view) {
        navigationViewModel.setMuted(toggleMute());

        for (OnClickListener onClickListener : onClickListeners) {
          onClickListener.onClick(view);
        }
      }
    });
  }
}
