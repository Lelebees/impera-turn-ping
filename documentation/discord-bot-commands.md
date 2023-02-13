## Definition

- ? : optional

## User commands

- `/link`
  Link a discord user to an impera account

- `/unlink`
  Unlink a discord user from an impera account

- `/track`
  args: `gameID`, ?`password`
  Will make the bot join the specified game if it hasn't started yet and the user is already in the game. If it has, the bot will tell the user that it is unable to join. Will also start logging notifications for every user in the game wich it is able and allowed to.

  Command can only be used if:

    - User is in the game

- `/notifications`
  args: ?`gameID`, ?`settings`

    - Allow users to set notification settings per game (`game` & `settings` argument)
    - Allow users to set notification settings globally (`settings` argument)
    - Allow users to see settings for a game (`game` argument)
    - Allow users to see (default) settings (no argument)

  `settings` values:

    - 0: No notifications
    - 1: DMs Only (overrides guild setting)
    - 2: DMs & Guild (Always sends to BOTH)
    - 3: Guild > DMs (If there is no guild, fall back to DMs)
    - 4: Guild only (If there is no guild, no message)

## Guild commands

These commands have different behaviour from user commands. This behaviour is invoked by the commands context: being used in a guild, rather then dm's.

- `/notifications`
  args: ?`gameID`, ?`channel`, ?`settings`

    - Allow admins to set notificationSettings per channel (`channel` & `settings` argument)
    - Allow admins to set notificationSettings per game (`game` & `settings` argument)
    - Allow admins to link a channel to a game (`channel` and `game` argument)
    - Allow admins to set notificationSettings globally (`settings` argument)
    - Allow admins to see settings for a channel (`channel` argument)
    - Allow admins to see settings for a game (`game` argument)
    - Allow admins to see (default) settings (no argument)

      Command can only be used if:

        - User has "Manage channels" permission
        - User is in the game (if `gameID` is present)

  `settings` values:

    - 0: No notifications (Players will still receive DMS, if set that way.)
    - 1: Notifications on

- `/track`
  args: `gameID`, ?`password`

  Will make the bot track a game in the guild it is used in, if the user that invoked the command is in the game, and has Manage Channels permission. `password` is ignored if the bot is already in the game, but not tracking for this guild yet.

  Command can only be used if:

    - User has "Manage channels" permission
    - User is in the game
