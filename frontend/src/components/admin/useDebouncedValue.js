import {useEffect, useState} from 'react';

/**
 * Returns `value` only after it has stopped changing for `delayMs`, so typing in a search box
 * does not fire one request per keystroke.
 */
const useDebouncedValue = (value, delayMs = 400) => {
    const [debounced, setDebounced] = useState(value);

    useEffect(() => {
        const timer = setTimeout(() => setDebounced(value), delayMs);
        return () => clearTimeout(timer);
    }, [value, delayMs]);

    return debounced;
};

export default useDebouncedValue;
