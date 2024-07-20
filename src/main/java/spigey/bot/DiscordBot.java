package spigey.bot;

import com.clarifai.channel.ClarifaiChannel;
import com.clarifai.credentials.ClarifaiCallCredentials;
import com.clarifai.grpc.api.*;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.interaction.command.CommandAutoCompleteInteractionEvent;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;
import net.dv8tion.jda.api.sharding.ShardManager;
import net.dv8tion.jda.api.utils.FileUpload;
import org.jetbrains.annotations.NotNull;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spigey.bot.system.*;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import spigey.bot.system.Timer;

import java.io.*;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import static spigey.bot.system.sys.*;
import static spigey.bot.system.util.*;
import static spigey.bot.system.util.msg;

public class DiscordBot extends ListenerAdapter {
    static CommandHandler commandHandler;

    private ShardManager shardManager;
    public static String prefix;
    public static String BotOwner = "1203448484498243645";
    public static final Logger log = LoggerFactory.getLogger(DiscordBot.class);
    /* public static JDA jda = DefaultShardManagerBuilder.createDefault(env.TOKEN)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
            .addEventListeners(new DiscordBot()).setShardsTotal(5).build().getShardById(2); */
    public static JDA jda = JDABuilder.createDefault(env.TOKEN)
            .enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS)
            .addEventListeners(new DiscordBot())
            .setActivity(Activity.watching("your posts"))
            .build();
    public static TextChannel console = null;
    public static Config config;

    /*
        package spigey.bot.system;

        public class env {
            public static final String TOKEN = "...";
            public static final String ENCRYPTION_KEY = "...";
        }
    */
    static {
        try {
            config = new Config(Files.readString(Paths.get("src/main/java/spigey/bot/config.json")));
        } catch (Exception e) {/**/}
    }
    private static final V2Grpc.V2BlockingStub stub = V2Grpc.newBlockingStub(ClarifaiChannel.INSTANCE.getGrpcChannel())
            .withCallCredentials(new ClarifaiCallCredentials(env.CLARIFAI));
    public static void main(String[] args) throws Exception {
        // JDABuilder.createDefault(env.DEV_TOKEN).enableIntents(GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MEMBERS).addEventListeners(new DiscordBot()).build();
        try {
            console = jda.getGuildById("1219338270773874729").getTextChannelById("1247203483652849726");
        }catch(Exception L){/**/}
        // db.setDefaultValue((String) config.get("DEFAULT_VALUE"));
        db.init("src/main/java/spigey/bot/system/database/", "0");
        prefix = (String) config.get("PREFIX");
        log("  _____  _                       _   ____        _   \n |  __ \\(_)                     | | |  _ \\      | |  \n | |  | |_ ___  ___ ___  _ __ __| | | |_) | ___ | |_ \n | |  | | / __|/ __/ _ \\| '__/ _` | |  _ < / _ \\| __|\n | |__| | \\__ \\ (_| (_) | | | (_| | | |_) | (_) | |_ \n |_____/|_|___/\\___\\___/|_|  \\__,_| |____/ \\___/ \\__|\n                                                     ");
        commandHandler = new CommandHandler();
        // register slash commands
        jda.updateCommands().addCommands(
                Commands.slash("register", "Register to be able to use the bot!")
                        .addOption(OptionType.STRING, "username", "Your username on SocialBot.", true)
                        .addOption(OptionType.STRING, "password", "Your password on SocialBot.", true),
                Commands.slash("login", "Login as any user with their username and password.")
                        .addSubcommands(
                                new SubcommandData("username", "Login using username and password.")
                                        .addOption(OptionType.STRING, "username", "Username to login to.", true)
                                        .addOption(OptionType.STRING, "password", "Password to login to.", true),
                                new SubcommandData("token", "Soon?")
                                        .addOption(OptionType.STRING, "token", "Token to login to.", true)
                        ),
                Commands.slash("setchannel", "Select a Channel to send posts to.")
                        .addOption(OptionType.CHANNEL, "channel", "Channel to send posts to", true),
                Commands.slash("post", "Post something to all servers.")
                        .addOption(OptionType.STRING, "content", "Text to post.", true)
                        .addOption(OptionType.ATTACHMENT, "attachment", "Attach something to your post.", false),
                Commands.slash("profile", "View someone's profile.")
                        .addOption(OptionType.STRING, "username", "Whose profile to view.", false),
                Commands.slash("logout", "Log out of the bot to log into another account."),
                Commands.slash("change-password", "Change your password.")
                        .addOption(OptionType.STRING, "old-password", "Your old password.", true)
                        .addOption(OptionType.STRING, "new-password", "Your new password.", true),
                Commands.slash("help", "Explore a list of available commands and their usage."),
                Commands.slash("ai", "Customize your personal AI.")
                        .addOption(OptionType.STRING, "option", "Self explanatory.", true, true)
                        .addOption(OptionType.STRING, "personality", "Select a personality for your personal AI.", false),
                Commands.slash("status", "Display the Bot's status."),
                Commands.slash("friends", "Add or remove friends.")
                        .addSubcommands(
                                new SubcommandData("add", "Add someone as a friend.")
                                        .addOption(OptionType.STRING, "username", "Username to add.", true),
                                new SubcommandData("remove", "Remove a friend.")
                                        .addOption(OptionType.STRING, "username", "Username of the friend to remove.", true),
                                new SubcommandData("list", "View your friends.")
                        ),
                Commands.slash("setstatus", "Set a status that will be displayed in your profile.")
                        .addOption(OptionType.STRING, "status", "Status. Leave this empty to remove your status."),
                Commands.slash("notifications", "View your notifications."),
                Commands.slash("report", "Report a bug or a user.")
                        .addOption(OptionType.STRING, "text", "User or bug.", true)
        ).queue();
        /* try (BufferedReader reader = new BufferedReader(new FileReader("src/main/java/spigey/bot/system/badwords.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                badWords.add(line.trim());
            }
        } catch (Exception e) {
            errInfo(e);
        } */
        filter.loadConfig();
    }


    @Override
    public void onReady(@NotNull ReadyEvent event) {
        System.setOut(new DiscordPrintStream(System.out, console));
        System.setErr(new DiscordPrintStream(System.err , console));
        sys.debug("Logged in as " + event.getJDA().getSelfUser().getName() + "!");
    }


    private static Map<String, String> conversations = new ConcurrentHashMap<>(1);
    StringBuilder sb = new StringBuilder();
    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        try{if(console == null) console = event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1247203483652849726");}catch(Exception L){error("Could not set console channel!");}
        // event.getJDA().getGuildById("835464812581486633").getTextChannelById("1063768951768756265").sendMessage("<@632607624742961153>").queue();
        db.add("properties", "msgs", 1);
        // if(event.getAuthor().getId().equals("1175367923506884661")) event.getMessage().reply(String.format("\"%s-\" :nerd:", event.getMessage().getContentRaw())).queue();
        try {
            if (event.getGuild().getId().equals("1219338270773874729"))
                if (Objects.equals(((TextChannel) event.getChannel()).getParentCategoryId(), "1246077622522351626") && !event.getAuthor().isBot() && !event.getChannel().getId().equals("1247203483652849726"))
                    event.getMessage().delete().queue();
                else if(event.getChannel().getId().equals("1247203483652849726")){
                    // CONSOLE HERE
                    String[] args = event.getMessage().getContentRaw().split(" ");
                    if(!(event.getAuthor() == event.getJDA().getSelfUser())) ln("> " + event.getMessage().getContentRaw());

                    if(args[0].equals("db")){
                        String user;
                        String log = "Invalid input.";
                        try{
                            user = event.getJDA().retrieveUserById(args[2]).complete().getAsTag();
                        }catch(Exception a){
                            try {
                                user = args[2];
                            }catch(Exception L){user = event.getAuthor().getId();}
                        }
                        if(args[1].equals("write")){
                            db.write(args[2], args[3], args[4]);
                            log = "Set " + args[3] + " to " + args[4] + " for user " + user + ".";
                        } else if(args[1].equals("read")){
                            log = "User " + user + " has " + db.read(args[2], args[3]) + " " + args[3] + ".";
                        } else if(args[1].equals("remove")) {
                            db.remove(args[2], args[3]);
                            log = "Removed " + args[3] + " for user " + user + ".";
                        } /* else if(args[1].equals("clean")){
                            log = "Successfully cleaned " + db.clean() + " empty database entries!";
                        } else if(args[1].equals("retrieve")){
                            event.getChannel().sendMessage("").addFiles(FileUpload.fromData(new ByteArrayInputStream(sys.encrypt(db.get(), env.ENCRYPTION_KEY).getBytes(StandardCharsets.UTF_8)), "database.json")).queue();
                            log = "";
                        } */ else if(args[1].equals("delete")){
                            db.deletePost(args[2]);
                            log = "ok doen";
                        }
                        else{sys.warn("Invalid input.");}
                        // event.getChannel().sendMessage("`" + log + "`").queue();
                        sys.debug(log);
                    } else if (args[0].equals("debug")) {
                        sys.debug(getExcept(args, 0, " "));
                    } else if (args[0].equals("warn")) {
                        sys.warn(getExcept(args, 0, " "));
                    } else if (args[0].equals("error")) {
                        sys.error(getExcept(args, 0, " "));
                    } else if(args[0].equals("now")){
                        event.getChannel().sendMessage("<t:" + (event.getMessage().getTimeCreated().toEpochSecond() + 2) + ":R>").queue();
                    }


                    if (args[0].equals("encrypt") && args.length == 1) {
                        Message.Attachment attachment = event.getMessage().getAttachments().getFirst();

                        attachment.retrieveInputStream().thenAccept(inputStream -> {
                            try {
                                String fileContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                                String encryptedContent = sys.encrypt(fileContent, env.ENCRYPTION_KEY);
                                InputStream encryptedInputStream = new ByteArrayInputStream(encryptedContent.getBytes(StandardCharsets.UTF_8));

                                event.getChannel().sendMessage("Here is your encrypted file:")
                                        .addFiles(FileUpload.fromData(encryptedInputStream, attachment.getFileName()))
                                        .queue(message -> {
                                            util.deleteIn(message, 5, TimeUnit.SECONDS);
                                            event.getMessage().delete().queue();
                                        });
                            } catch (Exception e) {
                                sys.errInfo(e);
                            }
                        }).exceptionally(throwable -> {
                            sys.errInfo((Exception) throwable);
                            return null;
                        });
                    }

                    if (args[0].equals("decrypt") && args.length == 1) {
                        Message.Attachment attachment = event.getMessage().getAttachments().getFirst();

                        attachment.retrieveInputStream().thenAccept(inputStream -> {
                            try {
                                String fileContent = new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                                String encryptedContent = sys.decrypt(fileContent, env.ENCRYPTION_KEY);
                                InputStream encryptedInputStream = new ByteArrayInputStream(encryptedContent.getBytes(StandardCharsets.UTF_8));

                                event.getChannel().sendMessage("")
                                        .addFiles(FileUpload.fromData(encryptedInputStream, attachment.getFileName()))
                                        .queue(message -> {
                                            util.deleteIn(message, 5, TimeUnit.SECONDS);
                                            event.getMessage().delete().queue();
                                        });
                            } catch (Exception e) {
                                sys.errInfo(e);
                            }
                        }).exceptionally(throwable -> {
                            sys.errInfo((Exception) throwable);
                            return null;
                        });
                    }



                    if(args[0].equals("ai")){
                        if(args[1].equals("reply")){
                            User user = event.getJDA().retrieveUserById(args[2]).complete();
                            user.openPrivateChannel().queue(channel ->
                                    channel.retrieveMessageById(channel.getLatestMessageId())
                                            .queue(message -> message.reply(sys.getAtLeast(args, 3, " ")).queue())
                            );

                            sys.debug("AI: " + getAtLeast(args, 3, " "));
                        }else if (args[1].equals("reset")){
                            conversations.remove(args[2]);
                            sys.debug("Removed conversation history with " + event.getJDA().retrieveUserById(args[2]).complete().getName() + ".");
                        } else if (args[1].equals("personality")) {
                            db.write(db.read(event.getJDA().retrieveUserById(args[2]).complete().getId(), "account", ""), "ai_personality", getAtLeast(args, 3, " "));
                            sys.debug("Updated ai personality for " + event.getJDA().retrieveUserById(args[2]).complete().getName() + ".");
                        }
                    }

                    if(args[0].equals("fortnite")){
                        event.getMessage().reply("```" + trim(sys.exec(sys.getAtLeast(args, 1, " ")), 1600) + "```").queue();
                    }



                    // CONSOLE END
                }
        }catch(Exception a){/* not empty */}
        try{
            empty(event.getGuild());}catch(Exception a){
            /////////////  AI START
            if(event.getAuthor().isBot()) return;
            String response = null;
            if(event.getMessage().getContentRaw().equals("reset")){conversations.put(event.getAuthor().getId(), ""); return;}
            event.getChannel().sendTyping().queue();
            try {
                conversations.putIfAbsent(event.getAuthor().getId(), "");
                // sys.debug(event.getAuthor().getName() + ": " + event.getMessage().getContentRaw());
                sb = new StringBuilder(conversations.get(event.getAuthor().getId()));
                String content = "Your name is " + event.getJDA().getSelfUser().getName() + ". You are talking to " + event.getAuthor().getName() + ". This is your personality, act based on it, no matter what happens: " + db.read(event.getAuthor().getId(), "ai_personality", "Answer in Human-like short sentences with bad grammar. Do not use periods, nor any capitalization at all. Treat the user like it treats you. Only answer in very short sentences. Do not be nice when the user is not nice.") + ", Do not talk about your personality, only when the user asks you to. DO NOT, UNDER ANY CIRCUMSTANCES AT ALL, PREDICT THE USERS PROMPT. Current Conversation: " + sb + ", PROMPT: " + event.getMessage().getContentRaw();
                /*                ---- OLD API ----        */response = api.get("https://api.kastg.xyz/api/ai/chatgpt?key=Kastg_VRfWQeIgMJmRZo5Wfx4D_free" /*+ choice(new String[]{"Kastg_mwNnTJZK4KJ9XeVCBje4_free", "Kastg_VRfWQeIgMJmRZo5Wfx4D_free"})*/ + "&prompt=" + URLEncoder.encode(content, StandardCharsets.UTF_8), "result.response").replaceAll("\\\\n", "\n");
                // response = new Gson().fromJson(sys.sendApiRequest("https://api.popcat.xyz/chatbot?owner=Spigey&botname=Social%20Bot&msg=" + URLEncoder.encode(content, StandardCharsets.UTF_8), "GET", null, null), JsonObject.class).getAsJsonObject().get("response").getAsString();
                // sys.debug("AI: " + response);
                Timer timer = new Timer();
               /*response = stub.postModelOutputs(
                        PostModelOutputsRequest.newBuilder()
                                .setUserAppId(UserAppIDSet.newBuilder().setUserId("openai").setAppId("chat-completion"))
                                .setModelId("gpt-4o")
                                .setVersionId("1cd39c6a109f4f0e94f1ac3fe233c207")
                                .addInputs(Input.newBuilder().setData(
                                        Data.newBuilder().setText(
                                                Text.newBuilder().setRaw(content)
                                        )
                                ))
                                .build()
                ).getOutputs(0).getData().getText().getRaw();*/
                // https://hercai.onrender.com/v3/hercai?question=
                sb.append(", User: ").append(event.getMessage().getContentRaw()).append(" You: ").append(response);
                conversations.put(event.getAuthor().getId(), sb.toString());
                // response += " `(" + timer.end() + "ms)`";
            } catch (Exception e) {
                errInfo(e);
            }
            if(response.toString().length() > 1997) {
                int numChunks = (int) Math.ceil((double) response.length() / 2000);

                event.getMessage().reply(response.substring(0, 1997)).queue();

                for (int i = 1; i < numChunks; i++) {
                    int start = i * 2000;
                    int end = Math.min(response.length(), (i + 1) * 2000);
                    event.getChannel().sendMessage(response.substring(start, end)).queue();
                }
            }
            else event.getMessage().reply(strOrDefault(response, "No response from AI.")).queue();
            //////////// AI END
        }
        BotOwner = "1203448484498243645"; // event.getJDA().retrieveApplicationInfo().complete().getOwner().getId();
        try {
            commandHandler.onMessageReceived(event);
        } catch (Exception e) {
            init(event, this);
            StringBuilder err = new StringBuilder(e + "\n   ");
            for(int i = 0; i < e.getStackTrace().length - 1; i++){
                err.append(e.getStackTrace()[i]).append("\n   ");
            }
            err.append(e.getStackTrace()[e.getStackTrace().length - 1]);
            error("A critical has occurred while executing Command:\n" + e + "\nMessage: " + event.getMessage().getContentRaw(), false);
            msg("A critical error occurred while executing Command: ```" + (err.toString().length() > 1000 ? err.substring(0, 1000) + "..." : err.toString()) + "```\nThis error has been automatically reported.");
            TextChannel channel = event.getJDA().getGuildById("1219338270773874729").getTextChannelById("1246091381659668521");
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Error Report")
                    .setDescription(String.format("Message: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`",event.getMessage().getContentRaw(), event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator(), event.getAuthor().getId()))
                    .setColor(EmbedColor.RED)
                    .build();
            try {
                Path temp = Files.createTempFile("error", ".txt");
                Files.writeString(temp, err);
            } catch (Exception L) {
                msg("<@" + authorId() + ">\nVery Critical error has occurred while trying to report critical error while trying to report error! Please report this error immediately to the bot owner.\nUsername: `" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getName() + "`\nID:`" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getId() + "`");
                msg("Shutting down bot...");
                exitWithError(String.format("VERY CRITICAL ERROR\n\n\nMessage: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`",event.getMessage().getContentRaw(), event.getAuthor().getName() + "#" + event.getAuthor().getDiscriminator(), event.getAuthor().getId()));
            }
            channel.sendMessage("<@" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getId() + ">").addEmbeds(embed).addFiles(FileUpload.fromData(err.toString().getBytes(StandardCharsets.UTF_8), "error_report.txt")).queue();
        }
    }

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event){
        try {
            commandHandler.onSlashCommand(event);
        } catch(Exception e){
            StringBuilder err = new StringBuilder(e + "\n   ");
            for(int i = 0; i < e.getStackTrace().length - 1; i++){
                err.append(e.getStackTrace()[i]).append("\n   ");
            }
            err.append(e.getStackTrace()[e.getStackTrace().length - 1]);
            error("A critical has occurred while executing Slash Command:\n" + e + "\nMessage: " + event.getName(), false);
            event.reply("A critical error occurred while executing Slash Command: ```" + (err.toString().length() > 1000 ? err.substring(0, 1000) + "..." : err.toString()) + "```\nThis error has been automatically reported.").queue();
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle("Error Report")
                    .setDescription(String.format("Message: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`",event.getName(), event.getUser().getName() + "#" + event.getUser().getDiscriminator(), event.getUser().getId()))
                    .setColor(EmbedColor.RED)
                    .build();
            try {
                Path temp = Files.createTempFile("error", ".txt");
                Files.writeString(temp, err);
            } catch (Exception L) {
                msg("<@" + authorId() + ">\nVery Critical error has occurred while trying to report critical error while trying to report error! Please report this error immediately to the bot owner.\nUsername: `" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getName() + "`\nID:`" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getId() + "`");
                msg("Shutting down bot...");
                sys.exitWithError(String.format("VERY CRITICAL ERROR\n\n\nMessage: ```%s```\nAuthor Username: `%s`\nAuthor ID: `%s`",event.getName(), event.getUser().getName() + "#" + event.getUser().getDiscriminator(), event.getUser().getId()));
            }
            event.reply("<@" + event.getJDA().retrieveApplicationInfo().complete().getOwner().getId() + ">").addEmbeds(embed).addFiles(FileUpload.fromData(err.toString().getBytes(StandardCharsets.UTF_8), "error_report.txt")).queue();
        }
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        try {
            commandHandler.onButton(event);
        } catch (IOException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onCommandAutoCompleteInteraction(@NotNull CommandAutoCompleteInteractionEvent event) {
        util.autoComplete("ai", "option", new String[]{"customize", "reset"}, event);
        // util.autoComplete("friends", "option", new String[]{"add", "remove", "list"}, event);
    }
}