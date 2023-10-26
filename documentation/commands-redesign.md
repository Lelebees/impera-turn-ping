Goals:

1. Simplify first-time setup. Ideally, the bot requires no setup.
2. Be proactive. A user using /track doesn't need to also use /notifications to get notifications.
3. The bot should be easy to use and learn, and the commands should be easy and intuitive to understand.

## New command definitions

### /link

Guild, DMs
Unchanged. Will link a user's discord account to their Impera account. If the user is already linked, it will tell them
to unlink first.

### /unlink

Guild, DMs
Unchanged. Will unlink a user.

### /track <gameid> <?channel>

Guild, DMs

Completely replaces the tracking functionality from /notifications.
The command will now decide if it is being used in a guild or dm context, and change its behaviour accordingly.
If the command is used in DMs, it will use the current behaviour.
If it is used in a guild, it will use the following behaviour:

1. The command will check if the user has the "Manage channels" permission. If they do, it will continue, otherwise it
   will tell them they don't have permission.
2. The command will check if the game is already being tracked in the guild. If it is, it skips to step 4.
3. The command will add the game to the tracked games list
4. The command will add the game/channel combination to the tracked channels list, in this case, that would be the
   channel it is used in.
   If the command is used with a channel set, it will substitute the channel it is used in for the specified channel.
   This behaviour does not work in DMs.
   If a default channel has been set, it will use that channel instead of the channel it is used in. This behaviour does
   not work in DMs, but also cannot throw an error for them.

Note: perhaps users may want to set different notification settings for different games. We'll see if it is required.

### /untrack <?gameid> <?channel>

Guild, DMs

Will remove the specified game/channel (combination(s)) from the tracked channels list: i.e.
If there is only a gameid specified, it will remove all combinations with the given gameid in this context from the
tracked channels list.
If there is only a channel specified, it will remove all combinations with the given channel in this context from the
tracked channels list.
If there is both a gameid and a channel specified, it will remove the combination of the given gameid and channel.

It is key to remember that this command must not remove a combination from the list if the gameid is present, but the
channel is not in the context (i.e. a separate guild or a user's DMs are the source, thus a 2nd guild shouldn't lose it'
s tracking.)

### /settings set <option> | /settings view

DMs (Guild - Ephemeral)

Replaces the settings functionality for users. This way, the guild command can remain guild only.

### /guildsettings set defaultChannel <channel> | /guildsettings view

Guild

Replaces the settings functionality for guilds and simplifies it.
This command can now only set the default channel, and can no longer track a game or be used to stop tracking one.

With the roles functionality, the commands /guildsettings set winnerRole <role> and /guildsettings set
permissionsRole <role> will be added.

