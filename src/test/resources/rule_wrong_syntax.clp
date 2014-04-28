(defrule DeviceLeft invalid
	?fact <-(left ?id ?where) 
	?device <-(device (id ?id) (where ?where)) 
=>
	(retract ?fact) 
	(modify ?device (available 0)) 
	(assert (event (id left) (value (str-cat ?where "/" ?id)))) 
	(assert (event (id alert) (value "Device left")))
)