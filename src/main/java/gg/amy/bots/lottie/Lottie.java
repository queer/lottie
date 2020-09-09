package gg.amy.bots.lottie;

import com.mewna.catnip.Catnip;
import com.mewna.catnip.CatnipOptions;
import com.mewna.catnip.shard.DiscordEvent;
import com.mewna.catnip.shard.GatewayIntent;
import gg.amy.bots.lottie.data.Database;
import gg.amy.bots.lottie.util.Env;
import gg.amy.bots.lottie.util.Translator;
import lombok.Getter;

/**
 * @author amy
 * @since 9/7/20.
 */
@Getter
public final class Lottie {
    private Catnip catnip;
    // TODO: Setup etc.
    private final Database database = new Database();

    private Lottie() {
    }

    public static void main(final String[] args) {
        new Lottie().start();
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void start() {
        Translator.preload();
        catnip = Catnip.catnip(new CatnipOptions(Env.TOKEN).apiVersion(7).intents(GatewayIntent.ALL_INTENTS));
        catnip.observable(DiscordEvent.MESSAGE_CREATE)
                .filter(m -> m.author().id().equals("128316294742147072"))
                .filter(m -> m.content().startsWith("!mod"))
                // TODO: Real command manager ^^;
                .subscribe(new ModCommand(this)::run);
        catnip.connect();
    }
}
