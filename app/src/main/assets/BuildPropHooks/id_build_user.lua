function after(hook, param)
	local ret = param:getResult()
	if ret == nil then
		return false
	end

    local fake = param:getSetting("build.USER")
    if fake == nil then
        fake = "god"
    end

    param:setResult(fake)
    return true, ret, fake
end