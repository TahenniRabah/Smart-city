import http from 'k6/http';
import { check, sleep } from 'k6';
import { Trend, Rate } from 'k6/metrics';

const ingestionDuration = new Trend('urbanhub_ingestion_duration', true);
const acceptedRate = new Rate('urbanhub_accepted');

export const options = {
  vus: 2,
  duration: '2m',
  thresholds: {
    http_req_failed: ['rate<0.01'],
    http_req_duration: ['p(95)<500'],
    urbanhub_accepted: ['rate>0.99'],
  },
};

const url = __ENV.URBANHUB_URL || 'http://localhost:8082/api/ingestion/measurements';
const apiKey = __ENV.INGESTION_API_KEY || 'urbanhub-local-development-key';

export default function () {
  const payload = JSON.stringify({
    zoneId: 'ZFE-1',
    stationId: `AIR-STATION-${String(__VU).padStart(3, '0')}`,
    indicator: ['NO2', 'PM10', 'PM25'][__ITER % 3],
    value: 100 + (__ITER % 200),
    timestamp: new Date().toISOString(),
  });

  const response = http.post(url, payload, {
    headers: {
      'Content-Type': 'application/json',
      'X-API-Key': apiKey,
    },
    tags: { scenario: 'smoke' },
  });

  const accepted = check(response, {
    'status is 202': (r) => r.status === 202,
    'correlationId exists': (r) => Boolean(r.json('correlationId')),
  });

  acceptedRate.add(accepted);
  ingestionDuration.add(response.timings.duration);
  sleep(1);
}
