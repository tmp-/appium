package io.appium.android.bootstrap.handler;

import android.text.method.Touch;
import com.android.uiautomator.core.UiObjectNotFoundException;
import io.appium.android.bootstrap.*;
import org.json.JSONException;
import android.os.SystemClock;
import android.view.KeyEvent;
import java.lang.reflect.Method;
import android.graphics.Rect;
import com.android.uiautomator.common.ReflectionUtils;
import com.android.uiautomator.core.UiObject;
import android.view.InputEvent;
import android.view.InputDevice;
import android.view.KeyCharacterMap;




/**
 * This handler is used to clear elements in the Android UI.
 *
 * Based on the element Id, clear that element.
 *
 * UiAutomator method clearText is flaky hence overriding it with custom implementation.
 */
public class Clear extends CommandHandler {

  /*
   * Trying to select entire text with correctLongClick and increasing time intervals.
   * Checking if element still has text in them and and if true falling back on UiAutomator clearText
   *
   * @param command The {@link AndroidCommand}
   *
   * @return {@link AndroidCommandResult}
   *
   * @throws JSONException
   *
   * @see io.appium.android.bootstrap.CommandHandler#execute(io.appium.android.
   * bootstrap.AndroidCommand)
   */
  @Override
  public AndroidCommandResult execute(final AndroidCommand command)
      throws JSONException {
    if (command.isElementCommand()) {
      try {
        final AndroidElement el = command.getElement();
        final ReflectionUtils utils = new ReflectionUtils();
        Rect rect = el.getVisibleBounds();
        TouchLongClick.correctLongClick(rect.right + 20, rect.centerY(),1000);
        // Trying to select entire text.

        // delete it
        final long eventTime = SystemClock.uptimeMillis();
        KeyEvent downEvent = new KeyEvent(eventTime, eventTime, KeyEvent.ACTION_DOWN,
                KeyEvent.KEYCODE_DEL, 0, 0, KeyCharacterMap.VIRTUAL_KEYBOARD, 0, 0,
                InputDevice.SOURCE_KEYBOARD);


        final Method injectInputEvent = utils.getMethodInjectInputEvent();

        Logger.debug("~~~~~~~~~~~~~~~~~~ method found: " + injectInputEvent.toString());

        //injectInputEvent(downEvent,false);
        // final Method sendKey = utils.getControllerMethod("sendKey", int.class,int.class);
        int count = 0;
        while (count < 50) {
           //sendKey.invoke(utils.getController(), KeyEvent.KEYCODE_DEL, 0);
            injectInputEvent.invoke(utils.getBridge(), downEvent, false);
            count++;
        }

        // If still text exist falling back on UIautomator clearText.
        if (!el.getText().isEmpty()) {
           Logger.debug("Clearing text not successful falling back to UiAutomator method clear");
           el.clearText();
        }
        return getSuccessResult(el.getText().isEmpty());
      } catch (final UiObjectNotFoundException e) {
        return new AndroidCommandResult(WDStatus.NO_SUCH_ELEMENT,
            e.getMessage());
      } catch (final Exception e) { // handle NullPointerException
          Logger.debug(e.toString());
        return getErrorResult("Unknown error clearing text");
      }
    }
    return getErrorResult("Unknown error");
  }
}
