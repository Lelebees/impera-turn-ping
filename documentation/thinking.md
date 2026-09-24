We want to send a ping

IF
- The channel is tracking the game
- The user is in the channel

AND
- The channel is NOT a DM 

- AND
  - The user wants to be pinged in servers
 
OR
- The channel is a DM

- AND
  - The user wants dm notifications (user notification setting: DMs Only, DMs and Guild)

- OR
  - There is no guild channel to ping this user in
  - AND
    - The user notification setting is Guild > DMs


Variables that influence what happens:
- Notification Setting
- Whether the channel is a DM or a Guild channel
- Whether the user to be pinged has access to the channel
- 