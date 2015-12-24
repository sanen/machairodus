var Queue = function(size) {
	this.capacity = size || 1000;        //队列最大容量
    this.queue = new Array();    //队列数据
    this._last;
    
    this.offer = function(value) {
    	if(!value || value == null)
    		return ;
    	
    	if(this.queue.length >= this.capacity)
    		this.queue.remove(0);
    	
    	this.queue.push(value);
    	this._last = value;
    };
    
    this.poll = function() {
    	if(this.queue.size > 0) {
        	var value = queue[0];
        	this.queue.remove(0);
        	return value;
    	} 
    	
    	return null;
    };
    
    this.get = function() {
    	return this.queue;
    };
    
    this.size = function() {
    	return this.queue.length;
    };
    
    this.isEmpty = function() {
    	return this.queue.length > 0;
    }
    
    this.clear = function() {
    	this.queue.length = 0;
    }
    
    this.last = function() {
    	return this._last;
    }
};

Array.prototype.remove = function(idx) {
    if (isNaN(idx) || idx > this.length) {
        return false;
    }

    for (var i = 0, n = 0; i < this.length; i++) {
        if (this[i] != this[idx]) {
            this[n++] = this[i];
        }
    }

	this.length -= 1;
}