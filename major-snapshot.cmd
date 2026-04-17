@ECHO OFF

ECHO +++++++++++++++++++++++++++++++  Maven Goal +++++++++++++++++++++++++++++++
ECHO        mvne ctemvn:auslieferung-maven-plugin:major-snapshot-versions
ECHO -------------------------------  Maven Goal -------------------------------
REM call mvne ctemvn:auslieferung-maven-plugin:help
REM pause
REM kurze Schreibweise, weil im POM das Plugin eingetragen wurde: call mvne auslieferung:major-snapshot-versions
call mvn ctemvn:auslieferung-maven-plugin:major-snapshot-versions
pause
