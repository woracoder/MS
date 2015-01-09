#include "vm/page.h"
#include <stdio.h>
#include <string.h>
#include "vm/frame.h"
#include "vm/swap.h"
#include "filesys/file.h"
#include "threads/malloc.h"
#include "threads/thread.h"
#include "userprog/pagedir.h"
#include "threads/vaddr.h"

/* Maximum size of process stack, in bytes. */
#define STACK_MAX (1024 * 1024)

static void page_destroy_func(struct hash_elem *, void *aux);

/* Destroys a page, which must be in the current process's
 page table.  Used as a callback for hash_destroy(). */
static void destroy_page(struct hash_elem *p_, void *aux UNUSED) {
	hash_destroy(p_, page_destroy_func);
}

static void page_destroy_func(struct hash_elem *e, void *aux UNUSED) {

}

/* Destroys the current process's page table. */
void page_exit(void) {
}

/* Returns the page containing the given virtual ADDRESS,
 or a null pointer if no such page exists.
 Allocates stack pages as necessary. */
struct page * page_for_addr(const void *address) {
	struct page pg;
	pg.addr = pg_round_down(address);
	struct hash_elem *e = hash_find(&thread_current()->supPageTable,
			&pg.hash_elem);
	if (!e) {
		return NULL;
	}
	return hash_entry(e, struct page, hash_elem);
}

/* Locks a frame for page P and pages it in.
 Returns true if successful, false on failure. */
static bool do_page_in(struct page *p) {
}

/* Faults in the page containing FAULT_ADDR.
 Returns true if successful, false on failure. */
bool page_in(void *fault_addr) {
}

/* Evicts page P.
 P must have a locked frame.
 Return true if successful, false on failure. */
bool page_out(struct page *p) {
}

/* Returns true if page P's data has been accessed recently,
 false otherwise.
 P must have a frame locked into memory. */
bool page_accessed_recently(struct page *p) {
}

void initializePageTable(struct hash *supPgTab) {
	hash_init(supPgTab, page_hash, page_less, NULL);
}

/* Adds a mapping for user virtual address VADDR to the page hash
 table.  Fails if VADDR is already mapped or if memory
 allocation fails. */
//page_allocate(void *vaddr, bool read_only) {
struct page * page_allocate(struct file *file, int32_t offsetAddress,
		void *vaddr, uint32_t readBytes, uint32_t zeroBytes, bool isWritable) {
	struct page *pg = malloc(sizeof(struct page));
	pg->file = file;
	pg->offsetAddress = offsetAddress;
	pg->addr = vaddr;
	pg->readBytes = readBytes;
	pg->zeroBytes = zeroBytes;
	pg->isWritable = isWritable;
	return hash_insert(&thread_current()->supPageTable, &pg->hash_elem);
}

/* Evicts the page containing address VADDR
 and removes it from the page table. */
void page_deallocate(void *vaddr) {
}

/* Returns a hash value for the page that E refers to. */
unsigned page_hash(const struct hash_elem *e, void *aux UNUSED) {
	const struct page *p = hash_entry(e, struct page, hash_elem);
	return hash_int((int) p->addr);
}

/* Returns true if page A precedes page B. */
bool page_less(const struct hash_elem *a_, const struct hash_elem *b_,
		void *aux UNUSED) {
	const struct page *a = hash_entry(a_, struct page, hash_elem);
	const struct page *b = hash_entry(b_, struct page, hash_elem);
	return (a->addr < b->addr);
}

/* Tries to lock the page containing ADDR into physical memory.
 If WILL_WRITE is true, the page must be writeable;
 otherwise it may be read-only.
 Returns true if successful, false on failure. */
bool page_lock(const void *addr, bool will_write) {
}

/* Unlocks a page locked with page_lock(). */
void page_unlock(const void *addr) {
}
