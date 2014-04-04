MapMeetServer
=============

Server/DB for Map Meet Android app

=============

Protocol: Client sends info about meeting, sent in as a JSON object.
*  Can send as many as they want, they will disconnect on their own.
*  Server then stores info in the database.
*  Client requesting info sends their name, queries db for info with
*  results having name in the list.
*  Server compiles a list of meetings and sends back a JSON list to the client.

*  Server info -- name:cscilab.bc.edu, socket #:10003, password:eagle id #
*  Database info -- server:localhost, username:watanami, password:mapmeet
