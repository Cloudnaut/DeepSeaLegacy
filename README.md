# DeepSeaLegacy
# Initially created: February 2015

Packetaufbau

Global:
4 Byte Magic beginning (DSCS)
1 Byte PacketType (	0=Discover;
			1=DiscoverReply;
			2=ConnectRequest;
			3=ConnectReply(Answer[Auflösung]);
			4=EstablishConnection(Auflösung);
			5=ImageFragment(Bilddaten, Zeile))


0, 1, 2:
Payload Empty

3: CLIENT
1 Byte Flags (ACCEPT, DENY)
4 Byte Screen Width
4 Byte Screen Height

4: SERVER
4 Byte FrameWidth
4 Byte FrameHeight

5: SERVER
4 Byte XPos
4 Byte YPos
4 Byte Length
? Byte Payload
