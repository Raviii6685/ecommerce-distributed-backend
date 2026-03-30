import json
from collections import defaultdict
from datetime import datetime

buckets = defaultdict(list)
errors = defaultdict(int)

print("Reading results...")

with open('/home/ravikhichar9715/stress-test-results-v5.json') as f:
    for line in f:
        try:
            d = json.loads(line)
            
            # Response times
            if d.get('metric') == 'http_req_duration' and d.get('type') == 'Point':
                val = d['data']['value']
                time = d['data']['time'][:16]  # per minute
                buckets[time].append(val)
            
            # Errors
            if d.get('metric') == 'http_req_failed' and d.get('type') == 'Point':
                time = d['data']['time'][:16]
                if d['data']['value'] == 1:
                    errors[time] += 1
                    
        except:
            pass

print("\n=== STRESS TEST ANALYSIS ===\n")
print(f"{'Time':<20} {'p95':>10} {'avg':>10} {'requests':>10} {'errors':>10} {'status':>10}")
print("-" * 75)

breaking_point = None

for t in sorted(buckets.keys()):
    vals = sorted(buckets[t])
    p95 = vals[int(len(vals)*0.95)]
    avg = sum(vals)/len(vals)
    err = errors.get(t, 0)
    
    if p95 > 2000 and breaking_point is None:
        breaking_point = t
        status = "❌ BREAKING!"
    elif p95 > 500:
        status = "⚠️  STRESSED"
    else:
        status = "✅ OK"
    
    print(f"{t:<20} {p95:>9.0f}ms {avg:>9.0f}ms {len(vals):>10} {err:>10} {status:>10}")

print("\n=== SUMMARY ===")
if breaking_point:
    print(f"Breaking point: {breaking_point}")
else:
    print("System did not break!")
    
all_vals = [v for vals in buckets.values() for v in vals]
all_vals.sort()
print(f"Overall p95: {all_vals[int(len(all_vals)*0.95)]:.0f}ms")
print(f"Overall avg: {sum(all_vals)/len(all_vals):.0f}ms")
print(f"Total requests: {len(all_vals)}")
