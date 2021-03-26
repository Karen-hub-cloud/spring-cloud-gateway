-- Redis Lua 脚本不会有并发问题么？
-- 因 Redis 是单线程模型，因此是线程安全的。

-- key方法参数，我们在RedisRateLimiter中塞进去了，replenishRate/burstCapacity
local tokens_key = KEYS[1]
local timestamp_key = KEYS[2]


-- argv 方法参数，分别为replenishRate/burstCapacity/1970至今毫秒数/消耗令牌桶数量
local rate = tonumber(ARGV[1])
local capacity = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])


-- 计算令牌桶填充满令牌需要多久时间，单位：秒。
local fill_time = capacity/rate
-- ttl = fill_time*2 保证时间充足
local ttl = math.floor(fill_time*2)


-- 调用 get 命令，获得令牌桶剩余令牌数(last_tokens) ，令牌桶最后填充令牌时间(last_refreshed) 。
local last_tokens = tonumber(redis.call("get", tokens_key))
if last_tokens == nil then
  last_tokens = capacity
end

local last_refreshed = tonumber(redis.call("get", timestamp_key))
if last_refreshed == nil then
  last_refreshed = 0
end


-- 填充令牌，计算新的令牌桶剩余令牌数( filled_tokens )。填充不超过令牌桶令牌上限。
local delta = math.max(0, now-last_refreshed)
local filled_tokens = math.min(capacity, last_tokens+(delta*rate))

-- Redis Lua 脚本不会有并发问题么？
-- 因 Redis 是单线程模型，因此是线程安全的。

-- key方法参数，我们在RedisRateLimiter中塞进去了，replenishRate/burstCapacity
local tokens_key = KEYS[1]
local timestamp_key = KEYS[2]


-- argv 方法参数，分别为replenishRate/burstCapacity/1970至今毫秒数/消耗令牌桶数量
local rate = tonumber(ARGV[1])
local capacity = tonumber(ARGV[2])
local now = tonumber(ARGV[3])
local requested = tonumber(ARGV[4])


-- 计算令牌桶填充满令牌需要多久时间，单位：秒。
local fill_time = capacity/rate
-- ttl = fill_time*2 保证时间充足
local ttl = math.floor(fill_time*2)


-- 调用 get 命令，获得令牌桶剩余令牌数(last_tokens) ，令牌桶最后填充令牌时间(last_refreshed) 。
local last_tokens = tonumber(redis.call("get", tokens_key))
if last_tokens == nil then
  last_tokens = capacity
end

local last_refreshed = tonumber(redis.call("get", timestamp_key))
if last_refreshed == nil then
  last_refreshed = 0
end


-- 填充令牌，计算新的令牌桶剩余令牌数( filled_tokens )。填充不超过令牌桶令牌上限。
local delta = math.max(0, now-last_refreshed)
local filled_tokens = math.min(capacity, last_tokens+(delta*rate))


-- 获取令牌是否成功。
-- 若成功，令牌桶剩余令牌数(new_tokens) 减消耗令牌数( requested )，并设置获取成功( allowed_num = 1 ) 。
-- 若失败，设置获取失败( allowed_num = 0 ) 。
local allowed = filled_tokens >= requested
local new_tokens = filled_tokens
local allowed_num = 0
if allowed then
  new_tokens = filled_tokens - requested
  allowed_num = 1
end


-- 设置令牌桶剩余令牌数( new_tokens ) ，令牌桶最后填充令牌时间(now)
redis.call("setex", tokens_key, ttl, new_tokens)
redis.call("setex", timestamp_key, ttl, now)

-- 返回数组结果，[是否获取令牌成功, 剩余令牌数] 。
return { allowed_num, new_tokens }

-- 获取令牌是否成功。
-- 若成功，令牌桶剩余令牌数(new_tokens) 减消耗令牌数( requested )，并设置获取成功( allowed_num = 1 ) 。
-- 若失败，设置获取失败( allowed_num = 0 ) 。
local allowed = filled_tokens >= requested
local new_tokens = filled_tokens
local allowed_num = 0
if allowed then
  new_tokens = filled_tokens - requested
  allowed_num = 1
end


-- 设置令牌桶剩余令牌数( new_tokens ) ，令牌桶最后填充令牌时间(now)
redis.call("setex", tokens_key, ttl, new_tokens)
redis.call("setex", timestamp_key, ttl, now)

-- 返回数组结果，[是否获取令牌成功, 剩余令牌数] 。
return { allowed_num, new_tokens }
