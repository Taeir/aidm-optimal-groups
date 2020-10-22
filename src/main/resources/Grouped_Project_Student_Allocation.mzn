% include "partition_set.mzn";
% include "all_different.mzn";
% include "int_set_channel.mzn";
% include "inverse_fn.mzn";
include "globals.mzn";


% GROUP SIZE BOUNDS
int: 'min group size';
int: 'max group size';
int: 'topic capacity';

int: 'max students with topic' = 'topic capacity' * 'max group size';
array[0..('max students with topic')] of bool: can_form_groups = array1d(0..25, [ true, false, false, false, true, true, false, false, true, true, true, false, true, true, true, true, true, true, true, true, true, true, true, true, true, true ]);

int: max_disutil = 4;

% META
int: '#students';

% STUDENTS
set of int: STUDENT = 1..'#students';

% TOPICS
int: '#topics';
set of int: TOPIC = 1..'#topics';

% STUDENT TOPIC PREFS
array[STUDENT, 1..'#topics'] of TOPIC: STUDENT_TOPIC_PREF;

% 
% Student's disutility of a topic is the ordinal position of the topic in its preferences
%    i.e. the 'inverse' of the preferences. minizinc inverse: f[x] = y <-> f^-1[y] = x
array[STUDENT, TOPIC] of var 1..'#topics': STUDENT_TOPIC_DISUTIL;
constraint forall(s in STUDENT) (
    row(STUDENT_TOPIC_DISUTIL, s) = inverse(row(STUDENT_TOPIC_PREF, s))
);

array[STUDENT] of var TOPIC: STUDENT_TOPIC_MATCHING;
array[TOPIC] of var set of STUDENT: TOPIC_STUDENT_MATCHING;
constraint int_set_channel(STUDENT_TOPIC_MATCHING, TOPIC_STUDENT_MATCHING);

   
constraint forall(t in TOPIC) (
    can_form_groups[card(TOPIC_STUDENT_MATCHING[t])]
);

constraint assert('max group size' - 'min group size' == 1, "Unsupported min-max groupsize delta");

%%%%%% MATCH_DISUTIL_BY_STUDENT: the disutility of each student of the proposed matching
array[STUDENT] of var int: MATCH_DISUTIL_BY_STUDENT;
constraint forall(student in STUDENT) (
    MATCH_DISUTIL_BY_STUDENT[student] = STUDENT_TOPIC_DISUTIL[student, STUDENT_TOPIC_MATCHING[student]]
);


array[TOPIC] of var set of STUDENT: MATCH_STUDENTS_BY_DISUTIL;
constraint int_set_channel(MATCH_DISUTIL_BY_STUDENT, MATCH_STUDENTS_BY_DISUTIL);


constraint forall(s in STUDENT)(
    MATCH_DISUTIL_BY_STUDENT[s] <= max_disutil
);

var 1..max_disutil: worst_disutil;
constraint worst_disutil = max(MATCH_DISUTIL_BY_STUDENT);


%%%%%% STD DEV
% var 0.0..('#students'*'#topics'): mean = sum(MATCH_DISUTIL_BY_STUDENT) / '#students';

% array[STUDENT] of var 0.0..('#topics'/2): ind_vary;
% constraint forall(s in STUDENT)(ind_vary[s] = trace("\(mean - MATCH_DISUTIL_BY_STUDENT[s])\n", mean - MATCH_DISUTIL_BY_STUDENT[s]));

% var 0.0..100.0: variance = sum(s in STUDENT)(ind_vary[s]);

% var 0.0..('#students'*'#topics'): std_dev = mean;%sqrt(variance);

% var float: obj = std_dev;


% TODO: Topic capacities

%%%%%% OBJECTIVE FUNCTIONS (to minimize):

%%% minimize the "average" disutility
var 0..('#students'*'#topics'): obj = sum(MATCH_DISUTIL_BY_STUDENT);

%%% min-max disutility (minimize greatest disutility)
%var '#students'..(max_disutil*1000+'#students'*max_disutil): obj = worst_disutil * 1000 + sum(MATCH_DISUTIL_BY_STUDENT);

%%% Pareto optimal min-max-disutility, factor 1000 is arbitrary
%     when a solution minimizing max disutility is found, the avg can still be improved!
%var int: obj = max(MATCH_DISUTIL_BY_STUDENT) * 1000 + sum(MATCH_DISUTIL_BY_STUDENT);

%%% AUPCR-lite
% var int: obj = sum(rank in 1..'#topics')(rank * card(MATCH_STUDENTS_BY_DISUTIL[rank]));

%%% Iffy
% var int: obj = sum(rank in 1..'#topics')(pow(rank, rank) * card(MATCH_STUDENTS_BY_DISUTIL[rank]));

% AUPCR

% var 1..('#topics'*'#students'): aupc = sum(rank in 1..'#topics')(sum(j in 1..rank)(card(MATCH_STUDENTS_BY_DISUTIL[j])));
% var 1..('#topics'*'#students'): total_area = '#topics' * '#students';

% var 0.0..1.0: aupcr = aupc / total_area;

% var float: obj = 1 - aupcr;

solve minimize obj :: int_search(MATCH_DISUTIL_BY_STUDENT, dom_w_deg, indomain_min);

% output [
%     "groups \t= \(GROUPS)\n",
%     "match  \t= \(GROUP_TOPIC_MATCHING)\n"
% ];

% output ["\nStudent's disutilities of matching:\n "] ++ ["| s(\(s)) = \(MATCH_DISUTIL_BY_STUDENT[s]) " | s in STUDENT] ++ ["|\n"];

% output ["\nNum students by disutil:\n "] ++ ["| d(\(d)) = \(card(MATCH_STUDENTS_BY_DISUTIL[d])) " | d in TOPIC] ++ ["|\n"];

% output ["\nNum students in topic:\n "] ++ ["| t(\(t)) = \(card(TOPIC_STUDENT_MATCHING[t])) " | t in TOPIC] ++ ["|\n"];

% output [ "\n", 
%     "obj \t= \(obj)\n",
%     "sum \t= \(sum(MATCH_DISUTIL_BY_STUDENT))\n",
% %     "mean \t= \(mean)\n", 
%     "max \t= \(max(MATCH_DISUTIL_BY_STUDENT))\n"
% %     "aupcr \t= \(aupcr) (= \(aupc) / \(total_area))\n"
%     %"std_dev \t= \(std_dev)\n"
% ];

output ["STUDENT_TOPIC_MATCHING = ", show(STUDENT_TOPIC_MATCHING)]
