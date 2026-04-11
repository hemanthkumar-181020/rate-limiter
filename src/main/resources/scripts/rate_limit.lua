-- rate_limit.lua

-- KEYS[1] = the Redis key e.g. "rate:192.168.1.1"
-- ARGV[1] = max requests allowed e.g. "10"
-- ARGV[2] = window duration in seconds e.g. "60"

local key = KEYS[1]
local limit = tonumber(ARGV[1])
local window = tonumber(ARGV[2])

-- Get current count for this key
local current = redis.call('GET', key)

if current == false then
    -- Key does not exist yet — first request from this client
    -- Set counter to 1, expire after window seconds
    redis.call('SET', key, 1, 'EX', window)
    return 1  -- return current count = 1 (allowed)

elseif tonumber(current) < limit then
    -- Under the limit — increment and allow
    redis.call('INCR', key)
    return tonumber(current) + 1  -- return new count (allowed)

else
    -- At or over limit — deny
    return -1  -- -1 means BLOCKED
end