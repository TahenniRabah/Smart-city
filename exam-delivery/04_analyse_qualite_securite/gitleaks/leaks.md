@'

Gitleaks version: 8.30.1

Scan scope: complete Git history

Secret values redacted: yes

Findings after qualification: 0

Gate policy: blocking

Result: PASS



A historical false positive was detected in a generated test report.

The generated file was removed from source control and ignored.

The historical exclusion is limited to the exact finding fingerprint.

'@ | Set-Content `

&#x20; exam-delivery/04\_analyse\_qualite\_securite/gitleaks/gitleaks-summary.txt `

&#x20; -Encoding utf8

