#include "vm/frame.h"
#include <stdio.h>
#include "vm/page.h"
#include "devices/timer.h"
#include "threads/init.h"
#include "threads/malloc.h"
#include "threads/palloc.h"
#include "threads/synch.h"
#include "threads/vaddr.h"

/* Initialize the frame manager. */
void frame_init(void) {
	list_init(&frameTable);
	lock_init(&frameTableLock);
}

/* Tries to allocate and lock a frame for PAGE.
 Returns the frame if successful, false on failure. */
struct frame * frame_alloc_and_lock(struct page *page) {
	struct frame *fr = malloc(sizeof(struct frame));
	fr->base = page->addr;
	fr->page = page;
	fr->t = thread_current();
	lock_acquire(&frameTableLock);
	list_push_back(&frameTable, &fr->listElement);
	lock_release(&frameTableLock);
	return fr;
}

/* Locks P's frame into memory, if it has one.
 Upon return, p->frame will not change until P is unlocked. */
void frame_lock(struct page *p) {

}

/* Releases frame F for use by another page.
 F must be locked for use by the current process.
 Any data in F is lost. */
void frame_free(struct frame *f) {

}

/* Unlocks frame F, allowing it to be evicted.
 F must be locked for use by the current process. */
void frame_unlock(struct frame *f) {

}
