// Every ATS with a polling implementation in the scheduler
// (the switch in scheduler/src/main/java/com/example/helpers/AtsFetchers.java).
// Subscription matching is done on the job's `ats` field, so these strings
// must match the fetcher case labels exactly.
export const ATS_LIST = [
  'greenhouse',
  'lever',
  'ashby',
  'smartrecruiters',
  'recruitee',
  'workable',
  'teamtailor',
  'workday',
  'remotive',
  'remoteok',
  'arbeitnow',
];
