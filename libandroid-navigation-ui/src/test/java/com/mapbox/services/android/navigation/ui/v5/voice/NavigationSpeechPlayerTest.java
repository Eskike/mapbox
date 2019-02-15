package com.mapbox.services.android.navigation.ui.v5.voice;

import android.media.MediaPlayer;

import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NavigationSpeechPlayerTest {

  @Test
  public void onPlayAnnouncement_mapboxSpeechPlayerIsGivenAnnouncement() {
    MapboxSpeechPlayer speechPlayer = mock(MapboxSpeechPlayer.class);
    NavigationSpeechPlayer navigationSpeechPlayer = buildNavigationSpeechPlayer(speechPlayer);
    SpeechAnnouncement announcement = mock(SpeechAnnouncement.class);

    navigationSpeechPlayer.play(announcement);

    verify(speechPlayer).play(announcement);
  }

  @Test
  public void onPlayAnnouncement_androidSpeechPlayerIsGivenAnnouncement() {
    AndroidSpeechPlayer speechPlayer = mock(AndroidSpeechPlayer.class);
    NavigationSpeechPlayer navigationSpeechPlayer = buildNavigationSpeechPlayer(speechPlayer);
    SpeechAnnouncement announcement = mock(SpeechAnnouncement.class);

    navigationSpeechPlayer.play(announcement);

    verify(speechPlayer).play(announcement);
  }

  @Test
  public void onIsMuted_returnsCorrectBooleanMuteValue() {
    MapboxSpeechPlayer speechPlayer = mock(MapboxSpeechPlayer.class);
    NavigationSpeechPlayer navigationSpeechPlayer = buildNavigationSpeechPlayer(speechPlayer);

    navigationSpeechPlayer.setMuted(true);

    assertTrue(navigationSpeechPlayer.isMuted());
  }

  @Test
  public void onSetMuted_speechPlayersAreSetMuted() {
    AndroidSpeechPlayer androidSpeechPlayer = mock(AndroidSpeechPlayer.class);
    MapboxSpeechPlayer mapboxSpeechPlayer = mock(MapboxSpeechPlayer.class);
    SpeechPlayerProvider speechPlayerProvider = new SpeechPlayerProvider(mapboxSpeechPlayer, androidSpeechPlayer);

    NavigationSpeechPlayer navigationSpeechPlayer = new NavigationSpeechPlayer(speechPlayerProvider);

    navigationSpeechPlayer.setMuted(true);

    verify(androidSpeechPlayer).setMuted(true);
    verify(mapboxSpeechPlayer).setMuted(true);
  }

  @Test
  public void onSetVolume_speechPlayersSetVolumeIsCalled() {
    AndroidSpeechPlayer androidSpeechPlayer = mock(AndroidSpeechPlayer.class);
    MediaPlayer mockedMediaPlayer = mock(MediaPlayer.class);
    MapboxSpeechPlayer mapboxSpeechPlayer = new MapboxSpeechPlayer(mockedMediaPlayer);
    SpeechPlayerProvider speechPlayerProvider = new SpeechPlayerProvider(mapboxSpeechPlayer, androidSpeechPlayer);

    NavigationSpeechPlayer navigationSpeechPlayer = new NavigationSpeechPlayer(speechPlayerProvider);

    navigationSpeechPlayer.setVolume(0.7f);

    verify(mockedMediaPlayer).setVolume(eq(0.7f), eq(0.7f));
  }

  // TODO Add AndroidSpeechPlayer set volume test

  @Test
  public void onOffRoute_providerOnOffRouteIsCalled() {
    SpeechPlayerProvider provider = mock(SpeechPlayerProvider.class);
    NavigationSpeechPlayer navigationSpeechPlayer = new NavigationSpeechPlayer(provider);

    navigationSpeechPlayer.onOffRoute();

    verify(provider).onOffRoute();
  }

  @Test
  public void onDestroy_providerOnDestroyIsCalled() {
    SpeechPlayerProvider provider = mock(SpeechPlayerProvider.class);
    NavigationSpeechPlayer navigationSpeechPlayer = new NavigationSpeechPlayer(provider);

    navigationSpeechPlayer.onOffRoute();

    verify(provider).onOffRoute();
  }

  private NavigationSpeechPlayer buildNavigationSpeechPlayer(SpeechPlayer speechPlayer) {
    SpeechPlayerProvider provider = mock(SpeechPlayerProvider.class);
    when(provider.retrieveMapboxSpeechPlayer()).thenReturn(speechPlayer);
    return new NavigationSpeechPlayer(provider);
  }
}
