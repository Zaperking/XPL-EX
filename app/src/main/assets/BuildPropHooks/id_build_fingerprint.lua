function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

    local fake = param:getSetting("build.FINGERPRINT")
    if fake == nil then
        fake = "google/angler/angler:8.1.0/OPM1.171019.011/4448085:user/release-keys"
    end

    param:setResult(fake)
    return true, ret, fake
end