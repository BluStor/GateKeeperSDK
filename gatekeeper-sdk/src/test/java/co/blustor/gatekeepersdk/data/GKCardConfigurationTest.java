package co.blustor.gatekeepersdk.data;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.IsEqual.equalTo;

public class GKCardConfigurationTest {
    @Test
    public void getConfigJsonReturnsAllConfigValuesAsJSON() {
        assertThat(new GKCardConfiguration("{\"authentication_to\":123}").getConfigJson(), is(equalTo("{\"authentication_to\":123}")));
        assertThat(new GKCardConfiguration(null).getConfigJson(), is(equalTo("{}")));
        assertThat(new GKCardConfiguration("").getConfigJson(), is(equalTo("{}")));
        assertThat(new GKCardConfiguration("this is not json").getConfigJson(), is(equalTo("{}")));

        GKCardConfiguration config = new GKCardConfiguration("{\"something\":82}");
        config.setAuthTimeout(321);
        assertThat(config.getConfigJson(), is(equalTo("{\"something\":82,\"authentication_to\":321}")));
    }

    @Test
    public void getAuthTimeoutReadsFromConfigValues() {
        GKCardConfiguration cardConfig = new GKCardConfiguration("{\"authentication_to\":123}");

        assertThat(cardConfig.getAuthTimeout(), is(equalTo(123)));
    }

    @Test
    public void getAuthTimeoutReturnsNullIfNotPresentInConfig() {
        GKCardConfiguration cardConfig = new GKCardConfiguration(null);

        assertThat(cardConfig.getAuthTimeout(), is(nullValue()));
    }

    @Test
    public void setAuthTimeoutAddsKeyToConfigIfNotPresent() {
        GKCardConfiguration cardConfig = new GKCardConfiguration(null);
        cardConfig.setAuthTimeout(3);

        assertThat(cardConfig.getAuthTimeout(), is(equalTo(3)));
    }

    @Test
    public void setAuthTimeoutUpdatesValueIfPresent() {
        GKCardConfiguration cardConfig = new GKCardConfiguration("{\"authentication_to\":123}");
        cardConfig.setAuthTimeout(3);

        assertThat(cardConfig.getAuthTimeout(), is(equalTo(3)));
    }

    @Test
    public void setAuthTimeoutRemovesKeyIfValueIsNull() {
        GKCardConfiguration cardConfig = new GKCardConfiguration("{\"authentication_to\":123}");
        cardConfig.setAuthTimeout(null);

        assertThat(cardConfig.mConfigValues.containsKey("authentication_to"), is(false));
    }
}
