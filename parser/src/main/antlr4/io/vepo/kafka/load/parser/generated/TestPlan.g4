grammar TestPlan;

plan: 'TestPlan' IDENTIFIER '{'
    attribute*
    connection
    step+
'}';
connection: 'connection' '{' attribute* '}';
attribute: IDENTIFIER ':' (value | propertyReference);
step: IDENTIFIER '{'
    '}';
propertyReference: '${' IDENTIFIER '}';
value: NUMBER | TIME_VALUE | ENUM_VALUE | MULTILINE_STRING | STRING | NULL;

NULL: 'null';
ENUM_VALUE: [A-Z]+;
IDENTIFIER: [A-Za-z][._\-A-Za-z0-9]*;
FILE_PATH: WINDOWS_FILE_PATH | UNIX_FILE_PATH;
WINDOWS_FILE_PATH: ([A-Z] ':\\' (FILENAME '\\')*)? (FILENAME '\\')+ FILENAME?;
UNIX_FILE_PATH: ('/' (FILENAME '/')*)? (FILENAME '/')+ FILENAME?;
STRING: DQUOTE (ESC | ~ ["\\])* DQUOTE;
MULTILINE_STRING: DQUOTE DQUOTE DQUOTE (ESC | '"' | ~["\\])* DQUOTE DQUOTE DQUOTE;
NUMBER: '-'? INT '.' [0-9] + EXP? | '-'? INT EXP | '-'? INT;
TIME_VALUE: INT TIME_UNIT;
WS: [ \t\r\n]+ -> skip;

fragment TIME_UNIT: ('h' | 'm' | 's' | 'ms');
fragment FILENAME: [._\-A-Za-z0-9]+;
fragment DQUOTE: '"';
fragment ESC: '\\' (["\\/bfnrt] | UNICODE);
fragment UNICODE: 'u' HEX HEX HEX HEX;
fragment HEX: [0-9a-fA-F];
fragment INT: '0' | [1-9] [0-9]*;// no leading zeros
fragment EXP: [Ee] [+\-]? INT; // \- since - means "range" inside [...]