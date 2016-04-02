package app.com.vaipo;

/**
 * Created by siddartha on 4/1/16.
 */
public interface ContactsListenerAction {

    void onContactsBtnClicked();

    void onContactsSelectedResult(Object result);

    void onActionCancel();
}
