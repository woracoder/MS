#ifndef VM_FRAME_H
#define VM_FRAME_H

#include <stdbool.h>
#include "threads/synch.h"
#include <list.h>

struct list frameTable;
struct lock frameTableLock;

/* A physical frame. */
struct frame {
	void *base; /* Kernel virtual base address. */
	struct page *page; /* Mapped process page, if any. */
/* ...............          other struct members as necessary */
	bool accessed;
	bool dirty;
	struct thread *t;
	struct list_elem listElement;
};

void frame_init(void);
struct frame *frame_alloc_and_lock(struct page *);
void frame_lock(struct page *);
void frame_free(struct frame *);
void frame_unlock(struct frame *);

#endif /* vm/frame.h */
