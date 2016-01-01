package app.com.vaipo.openTok;

import com.opentok.android.Publisher;
import com.opentok.android.Subscriber;

/**
 * Created by siddartha on 12/29/15.
 */
public interface ITalkUICallbacks {
    void addSubscribeView(Subscriber subscriber);
    void removeSubscribeView(Subscriber subscriber);
    void removeAllSubscribeView(Subscriber subscriber);

    void addPublisherView(Publisher publisher);
    void removePublisherView(Publisher publisher);
    void removeAllPublisherView(Publisher publisher);

    void addPreview(Publisher publisher);

    void swapCamera(Publisher publisher);
    void end();
    void attachSubscriberView(Subscriber subscriber);
    void attachPublisherView(Publisher publisher);

}
