-- define
set serveroutput on size unlimited;
declare
-- arrival
  mintime_arrival float;
  maxtime_arrival float;
  minjdate_arrival number;
  maxjdate_arrival number;
-- wfdisc
  mintime_wfdisc float;
  maxtime_wfdisc float;
  mintime_inarrivals_wfdisc float;
  maxtime_inarrivals_wfdisc float;
  minendtime_wfdisc float;
  maxendtime_wfdisc float; 
  minjdate_wfdisc number;
  maxjdate_wfdisc number;
begin
-- arrival
  SELECT min(time) INTO mintime_arrival FROM ARRIVAL;
  SELECT max(time) INTO maxtime_arrival FROM ARRIVAL;
  SELECT min(jdate) INTO minjdate_arrival FROM ARRIVAL;
  SELECT max(jdate) INTO maxjdate_arrival FROM ARRIVAL;
-- wfdisc
  SELECT min(time) INTO mintime_wfdisc FROM WFDISC;
  SELECT max(time) INTO maxtime_wfdisc FROM WFDISC;
  SELECT min(time) INTO mintime_inarrivals_wfdisc FROM WFDISC WHERE time >= mintime_arrival;
  SELECT max(time) INTO maxtime_inarrivals_wfdisc FROM WFDISC WHERE time <= maxtime_arrival;
  SELECT min(endtime) INTO minendtime_wfdisc FROM WFDISC;
  SELECT max(endtime) INTO maxendtime_wfdisc FROM WFDISC;
  SELECT min(jdate) INTO minjdate_wfdisc FROM WFDISC;
  SELECT max(jdate) INTO maxjdate_wfdisc FROM WFDISC;

-- arrival output
  DBMS_OUTPUT.PUT_LINE('MINTIME_ARRIVAL: '||etoh(mintime_arrival));
  DBMS_OUTPUT.PUT_LINE('MAXTIME_ARRIVAL: '||etoh(maxtime_arrival));
  DBMS_OUTPUT.PUT_LINE('MINJDATE_ARRIVAL: '||jtoh(minjdate_arrival));
  DBMS_OUTPUT.PUT_LINE('MAXJDATE_ARRIVAL: '||jtoh(maxjdate_arrival));
-- wfdisc output
  DBMS_OUTPUT.PUT_LINE(' MINTIME_WFDISC: '||etoh(mintime_wfdisc));
  DBMS_OUTPUT.PUT_LINE('MAXTIME_WFDISC: '||etoh(maxtime_wfdisc));
  DBMS_OUTPUT.PUT_LINE(' MINENDTIME_WFDISC: '||etoh(minendtime_wfdisc));
  DBMS_OUTPUT.PUT_LINE(' MAXENDTIME_WFDISC: '||etoh(maxendtime_wfdisc));
  DBMS_OUTPUT.PUT_LINE('MINJDATE_WFDISC: '||jtoh(minjdate_wfdisc));
  DBMS_OUTPUT.PUT_LINE('MAXJDATE_WFDISC: '||jtoh(maxjdate_wfdisc));
-- wfdisc in arrivals?
  DBMS_OUTPUT.PUT_LINE(' MINTIME_INARRIVALS_WFDISC: '||etoh(mintime_inarrivals_wfdisc));
  DBMS_OUTPUT.PUT_LINE('MAXTIME_INARRIVALS_WFDISC: '||etoh(maxtime_inarrivales_wfdisc));
-- channels and stations in this time frame?

end;
/