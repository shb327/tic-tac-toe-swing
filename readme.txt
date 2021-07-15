1.Detailed Description Of Implementation
    1.1)The most important part of my this project is Server. So, I'll start my explanation with it. By default it's run
    on port 25565, but it can be changed and I explain it in "How To Run" section. Once Server is started it will basically
    run forever, or until is stopped, and accept new clients, because of while(true) loop.
    One of the special feature of my Server is it's ability to broadcast(take a look at the Server Constructor and class
    Broadcaster). Broadcaster uses UDP to run and is used to implement Viewer. For further information check 1.3. After
    someone initiate new connect to my Server, it will be treated as a player and have it own id. After each user is successfully
    connected, it will be transferred to PlayerHandler. By project criterias I was supposed to have commands like "PLAY",
    "LOGOUT", and "LIST". All three of them are successfully implemented and I also have additional command called "SET",
    which I use to put player's sign (cross or circle) on the game board, and "UPDATE", than used send users info about changing
    a state of game(such as game map, player who turn and win status). After command "PLAY" is called, player will be added
    to Game Queue and he/she will have to wait till some other player will decide to play and only after that, game between those
    two will be started. "LOGOUT" keyword is automatically called after game is finished and what it does is simply removes
    finished game from the games list, and both players from the list of players and disconnects them from server. And finally
    key word "LIST" goes through all of the players in the map and simply sends sequence of Player's toStrings to Client, who asked
    for it.

    1.2)Next, but not least important one is Client. From the Client's perspective of view, everything is realized in User Interface.
    After window appears there will be not really much to do, before server's HostName is typed down and button "Connect" is pressed.
    Only after that Player will be connected to corresponding Server and he/she will have to wait until some other player will do
    exactly same thing. After game is started both players take turns in putting their's assigned symbols on the game field. After
    the assigned game is finished both players will see the result in pop up window will be disconnected and program for them
    will be successfully executed. Also, in the left top corner of yellow area User has button, called "List", which shows
    all of the Players that are currently connected to Server(please notice that, this button becomes able only after User is connected).

    1.3)Viewer is the easiest entity of my program. It simply connects to same Host, as Server is running on, but on other port (broadcaster port)
    and then get's information about everything that happens on server, including information about all players and all games.
    it's done using UDP type of connection, which allows quick and reliable data transfer in terms of Datagrams.



2.How To Compile
    Because I use build-automation system called Gradle in my project, it's build is extremely simple. To build the program
    you simply need to open file called "build.bat" or just type in following command in terminal: "gradlew build".



3.How To Run
    3.1)To run server, just open file called "server.bat" (you can set ports as an arguments, first one for the server and another
    one for broadcaster. if you won't do this, server will simply run on default port: 25565). Running is done by calling
    Gradle command: (:server:run --args "%1 %2").

    3.2)To run client, just open file called "client.bat" (you can set port as an arguments, if you won't do this,
    server will simply run on default port: 25565). Further running process is called by calling Gradle command: (:server:run).

    3.3)To run viewer, just open file called "viewer.bat" (you can set ports as an arguments, if you won't do this,
     server will simply run on default port: 25565). Further running process is called by calling Gradle command: (:viewer:run --args "%1").



4.How To Use
    Honestly, there is not much to tell about usage perspective for Server or Viewer, because they simply need to be correctly run and
    have right Host Names. However, after Client is successfully run, User has to put IP address and Host Name in the Text Field marked
    as "IP".(Example of input: "127.0.0.1:25565"), after that User can play versus his/her opponent. Game is help according to standard
    tic-tac-toe rules. After game is finished, User will get pop-up window with the result of the game. After that, game
    is officially finished and window may be finished. In any moment of the game it will be possible to click on button "List" and see
    all of the players currently connected to the game(Notice, that it's not possible to click that button until user himself/herself
    is connected to the Server).



5.What Does Not Work
    I have put a lot of time and effort into this project. I have fixed all possible errors, I can think of. However, I would
    recommend running this project on JDK version 1.8, due to backword support of java(in 1.9+ versions javafx removed by default).
    Also, I noticed that on some machines Fonts of User Interface ware not used (Based on my observation, fonts does not work
    on Macs with OS Catalina version lower than 10.15.2 and on Windows computers without activated System).

