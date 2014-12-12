create table `sessions` (
  `beginstring`  char(8) not null,
  `sendercompid` varchar(64) not null,
  `sendersubid`  varchar(64) not null,
  `senderlocid`  varchar(64) not null,
  `targetcompid` varchar(64) not null,
  `targetsubid` varchar(64) not null,
  `targetlocid`  varchar(64) not null,
  `session_qualifier` varchar(64) not null,
  `creation_time` timestamp not null,
  `incoming_seqnum` integer not null,
  `outgoing_seqnum` integer not null,
  primary key (`beginstring`, `sendercompid`, `sendersubid`, `senderlocid`,
               `targetcompid`, `targetsubid`, `targetlocid`, `session_qualifier`)
);

create table `messages` (
  `beginstring` char(8) not null,
  `sendercompid` varchar(64) not null,
  `sendersubid` varchar(64) not null,
  `senderlocid` varchar(64) not null,
  `targetcompid` varchar(64) not null,
  `targetsubid` varchar(64) not null,
  `targetlocid` varchar(64) not null,
  `session_qualifier` varchar(64) not null,
  `msgseqnum` integer not null,
  `message` text not null,
  primary key (`beginstring`, `sendercompid`, `sendersubid`, `senderlocid`,
               `targetcompid`, `targetsubid`, `targetlocid`, `session_qualifier`,
               `msgseqnum`)
);